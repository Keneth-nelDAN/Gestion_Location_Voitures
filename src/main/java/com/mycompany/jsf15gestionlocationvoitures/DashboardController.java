package com.mycompany.jsf15gestionlocationvoitures;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import jakarta.annotation.Resource;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceUnit;
import jakarta.transaction.UserTransaction;

@Named("dashboardController")
@RequestScoped
public class DashboardController implements Serializable {

    @Resource
    private UserTransaction utx = null;
    @PersistenceUnit(unitName = "my_persistence_unit")
    private EntityManagerFactory emf = null;

    private List<Location> locations;
    private List<Client> clients;
    private List<Vehicule> vehicles;
    private List<Retour> returns;

    private List<Location> locations() {
        if (locations == null) {
            locations = new LocationJpaController(utx, emf).findLocationEntities();
        }
        return locations;
    }

    private List<Client> clients() {
        if (clients == null) {
            clients = new ClientJpaController(utx, emf).findClientEntities();
        }
        return clients;
    }

    private List<Vehicule> vehicles() {
        if (vehicles == null) {
            vehicles = new VehiculeJpaController(utx, emf).findVehiculeEntities();
        }
        return vehicles;
    }

    private List<Retour> returns() {
        if (returns == null) {
            returns = new RetourJpaController(utx, emf).findRetourEntities();
        }
        return returns;
    }

    public int getClientCount() {
        return clients().size();
    }

    public int getVehiculeCount() {
        return vehicles().size();
    }

    public int getAvailableVehiculeCount() {
        int count = 0;
        for (Vehicule vehicule : vehicles()) {
            String statut = normalize(vehicule.getStatutDisponible());
            if (statut.isEmpty() || statut.equals("disponible") || statut.equals("oui") || statut.equals("true") || statut.equals("libre")) {
                count++;
            }
        }
        return count;
    }

    public int getLocationCount() {
        return locations().size();
    }

    public int getActiveLocationCount() {
        int count = 0;
        for (Location location : locations()) {
            if (isActive(location)) {
                count++;
            }
        }
        return count;
    }

    public int getRetourCount() {
        return returns().size();
    }

    public long getTotalRevenue() {
        long total = 0;
        for (Location location : locations()) {
            total += revenueOf(location);
        }
        return total;
    }

    public int getAverageDuration() {
        if (locations().isEmpty()) {
            return 0;
        }
        long total = 0;
        int counted = 0;
        for (Location location : locations()) {
            if (location.getDateDebutPrevue() != null && location.getDateFinPrevue() != null) {
                long days = (location.getDateFinPrevue().getTime() - location.getDateDebutPrevue().getTime()) / 86400000L;
                if (days >= 0) {
                    total += days;
                    counted++;
                }
            }
        }
        return counted == 0 ? 0 : (int) Math.round((double) total / counted);
    }

    public List<Location> getUpcomingLocations() {
        Date now = new Date();
        List<Location> result = new ArrayList<>();
        for (Location location : locations()) {
            if (location.getDateFinPrevue() == null || !location.getDateFinPrevue().before(now)) {
                result.add(location);
            }
        }
        result.sort(Comparator.comparing(Location::getDateDebutPrevue, Comparator.nullsLast(Comparator.naturalOrder())));
        if (result.size() > 8) {
            return new ArrayList<>(result.subList(0, 8));
        }
        return result;
    }

    public String getMonthlyLabelsJson() {
        YearMonth[] months = months();
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < months.length; i++) {
            if (i > 0) {
                sb.append(",");
            }
            String label = months[i].format(DateTimeFormatter.ofPattern("MMM", Locale.FRENCH)).replace(".", "");
            if (!label.isEmpty()) {
                label = label.substring(0, 1).toUpperCase(Locale.FRENCH) + label.substring(1);
            }
            sb.append("\"").append(json(label)).append("\"");
        }
        return sb.append("]").toString();
    }

    public String getMonthlyLocationsJson() {
        YearMonth[] months = months();
        int[] values = new int[months.length];
        for (Location location : locations()) {
            YearMonth month = monthOf(location.getDateCreation() != null ? location.getDateCreation() : location.getDateDebutPrevue());
            int index = indexOf(months, month);
            if (index >= 0) {
                values[index]++;
            }
        }
        return numbers(values);
    }

    public String getMonthlyRevenueJson() {
        YearMonth[] months = months();
        long[] values = new long[months.length];
        for (Location location : locations()) {
            YearMonth month = monthOf(location.getDateCreation() != null ? location.getDateCreation() : location.getDateDebutPrevue());
            int index = indexOf(months, month);
            if (index >= 0) {
                values[index] += revenueOf(location);
            }
        }
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < values.length; i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append(values[i]);
        }
        return sb.append("]").toString();
    }

    public String getStatusLabelsJson() {
        StringBuilder sb = new StringBuilder("[");
        boolean first = true;
        for (String label : statusCounts().keySet()) {
            if (!first) {
                sb.append(",");
            }
            sb.append("\"").append(json(label)).append("\"");
            first = false;
        }
        return sb.append("]").toString();
    }

    public String getStatusCountsJson() {
        StringBuilder sb = new StringBuilder("[");
        boolean first = true;
        for (Integer value : statusCounts().values()) {
            if (!first) {
                sb.append(",");
            }
            sb.append(value);
            first = false;
        }
        return sb.append("]").toString();
    }

    public String getTopVehicleLabelsJson() {
        StringBuilder sb = new StringBuilder("[");
        boolean first = true;
        for (Map.Entry<String, Integer> entry : topVehicles().entrySet()) {
            if (!first) {
                sb.append(",");
            }
            sb.append("\"").append(json(entry.getKey())).append("\"");
            first = false;
        }
        return sb.append("]").toString();
    }

    public String getTopVehicleCountsJson() {
        StringBuilder sb = new StringBuilder("[");
        boolean first = true;
        for (Map.Entry<String, Integer> entry : topVehicles().entrySet()) {
            if (!first) {
                sb.append(",");
            }
            sb.append(entry.getValue());
            first = false;
        }
        return sb.append("]").toString();
    }

    public String getLocationEventsJson() {
        StringBuilder sb = new StringBuilder("[");
        boolean first = true;
        List<Location> sorted = new ArrayList<>(locations());
        sorted.sort(Comparator.comparing(Location::getDateDebutPrevue, Comparator.nullsLast(Comparator.naturalOrder())));
        for (Location location : sorted) {
            String start = iso(location.getDateDebutPrevue());
            if (start == null) {
                continue;
            }
            String end = iso(location.getDateFinPrevue());
            if (end == null) {
                end = start;
            }
            String client = clientName(location);
            String vehicle = vehicleName(location);
            String title = "#" + location.getIdLocation() + " - " + client + (vehicle.isEmpty() ? "" : " (" + vehicle + ")");
            String color = isActive(location) ? "#3a57e8" : "#4bc7d2";
            if (!first) {
                sb.append(",");
            }
            sb.append("{\"id\":\"loc-").append(location.getIdLocation())
                    .append("\",\"title\":\"").append(json(title))
                    .append("\",\"start\":\"").append(start)
                    .append("\",\"end\":\"").append(end)
                    .append("\",\"backgroundColor\":\"").append(color)
                    .append("\",\"borderColor\":\"").append(color)
                    .append("\",\"textColor\":\"#ffffff\",\"extendedProps\":{\"statut\":\"")
                    .append(json(statusOf(location)))
                    .append("\",\"client\":\"").append(json(client))
                    .append("\",\"vehicule\":\"").append(json(vehicle))
                    .append("\"}}");
            first = false;
        }
        return sb.append("]").toString();
    }

    private YearMonth[] months() {
        YearMonth[] months = new YearMonth[6];
        YearMonth now = YearMonth.now();
        for (int i = 0; i < months.length; i++) {
            months[i] = now.minusMonths(months.length - 1 - i);
        }
        return months;
    }

    private int indexOf(YearMonth[] months, YearMonth month) {
        if (month == null) {
            return -1;
        }
        for (int i = 0; i < months.length; i++) {
            if (months[i].equals(month)) {
                return i;
            }
        }
        return -1;
    }

    private YearMonth monthOf(Date date) {
        if (date == null) {
            return null;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return YearMonth.of(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1);
    }

    private String numbers(int[] values) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < values.length; i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append(values[i]);
        }
        return sb.append("]").toString();
    }

    private Map<String, Integer> statusCounts() {
        Map<String, Integer> counts = new LinkedHashMap<>();
        for (Location location : locations()) {
            String status = statusOf(location);
            Integer current = counts.get(status);
            counts.put(status, current == null ? 1 : current + 1);
        }
        if (counts.isEmpty()) {
            counts.put("Aucune location", 0);
        }
        return counts;
    }

    private Map<String, Integer> topVehicles() {
        Map<String, Integer> counts = new LinkedHashMap<>();
        for (Location location : locations()) {
            String name = vehicleName(location);
            if (name.isEmpty()) {
                name = "Véhicule #" + (location.getIdVehicule() == null ? "?" : location.getIdVehicule().getIdVehicule());
            }
            Integer current = counts.get(name);
            counts.put(name, current == null ? 1 : current + 1);
        }
        List<Map.Entry<String, Integer>> entries = new ArrayList<>(counts.entrySet());
        entries.sort((a, b) -> {
            int comparison = Integer.compare(b.getValue(), a.getValue());
            return comparison == 0 ? a.getKey().compareTo(b.getKey()) : comparison;
        });
        Map<String, Integer> result = new LinkedHashMap<>();
        for (int i = 0; i < Math.min(5, entries.size()); i++) {
            result.put(entries.get(i).getKey(), entries.get(i).getValue());
        }
        if (result.isEmpty()) {
            result.put("Aucune donnée", 0);
        }
        return result;
    }

    private String clientName(Location location) {
        if (location.getIdClient() == null) {
            return "Client";
        }
        String first = location.getIdClient().getPrenom() == null ? "" : location.getIdClient().getPrenom();
        String last = location.getIdClient().getNom() == null ? "" : location.getIdClient().getNom();
        String name = (first + " " + last).trim();
        return name.isEmpty() ? "Client" : name;
    }

    private String vehicleName(Location location) {
        if (location.getIdVehicule() == null) {
            return "";
        }
        String plate = location.getIdVehicule().getImmatriculation();
        return plate == null ? "" : plate.trim();
    }

    private String statusOf(Location location) {
        String statut = location.getStatutLocation();
        String normalized = normalize(statut);
        if (location.getRetour() != null || normalized.contains("termine") || normalized.contains("retourne") || normalized.contains("clos")) {
            return "Terminée";
        }
        if (normalized.isEmpty()) {
            return "En cours";
        }
        return statut.trim();
    }

    private boolean isActive(Location location) {
        if (location.getRetour() != null) {
            return false;
        }
        String statut = normalize(location.getStatutLocation());
        if (statut.isEmpty()) {
            return true;
        }
        return statut.contains("en cours") || statut.contains("encours") || statut.contains("actif") || statut.contains("active")
                || statut.contains("location") || statut.contains("reserve") || statut.contains("attente");
    }

    private long revenueOf(Location location) {
        if (location.getMontantEstime() != null) {
            return location.getMontantEstime();
        }
        if (location.getTarifJournalierApplique() != null && location.getDateDebutPrevue() != null && location.getDateFinPrevue() != null) {
            long days = (location.getDateFinPrevue().getTime() - location.getDateDebutPrevue().getTime()) / 86400000L;
            return Math.max(0, days) * location.getTarifJournalierApplique();
        }
        return 0;
    }

    private String iso(Date date) {
        if (date == null) {
            return null;
        }
        return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(date);
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }
        String decomposed = java.text.Normalizer.normalize(value, java.text.Normalizer.Form.NFD);
        return decomposed.replaceAll("\\p{InCombiningDiacriticalMarks}+", "").toLowerCase(Locale.ROOT).trim();
    }

    private String json(String value) {
        if (value == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder(value.length());
        for (char c : value.toCharArray()) {
            switch (c) {
                case '\\':
                    sb.append("\\\\");
                    break;
                case '"':
                    sb.append("\\\"");
                    break;
                case '\n':
                    sb.append("\\n");
                    break;
                case '\r':
                    sb.append("\\r");
                    break;
                case '\t':
                    sb.append("\\t");
                    break;
                case '<':
                    sb.append("\\u003c");
                    break;
                case '>':
                    sb.append("\\u003e");
                    break;
                case '&':
                    sb.append("\\u0026");
                    break;
                default:
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
            }
        }
        return sb.toString();
    }
}
