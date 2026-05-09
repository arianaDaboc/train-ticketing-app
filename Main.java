import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
public class Main {
    private static final Scanner scanner = new Scanner(System.in);
      private static final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
     private static final Map<String, Route> routes = new LinkedHashMap<>();
       private static final Map<String, Train> trains = new LinkedHashMap<>();
    public static void main(String[] args) {
        seedData();
         while (true) {
            printMainMenu();
            String choice = scanner.nextLine().trim();
             if (choice.equals("1")) {
                handleBooking();
             }else if (choice.equals("2")) {
                handleFindConnections();
            }else if (choice.equals("3")) {
                    handleAdminMenu();
            }else if (choice.equals("0")) {
                 System.out.println("Goodbye.");
                break;
            } else {
               System.out.println("Invalid option.");
            }
             System.out.println();
        }
    }
    private static void printMainMenu() {
       System.out.println("==== Train Ticketing ====");
       System.out.println("1. Book ticket(s)");
        System.out.println("2. Find departure/arrival times");
      System.out.println("3. Administrator menu");
        System.out.println("0. Exit");
     System.out.print("Choose option: ");
    }
    private static void handleBooking() {
        listTrains();
       System.out.print("Train ID: ");
       String trainId = scanner.nextLine().trim();
        Train train = trains.get(trainId);
        if (train == null) {
         System.out.println("Train not found.");
         return;
        }
        printTrainStations(train);
        System.out.print("From station: ");
        String from = scanner.nextLine().trim();
        System.out.print("To station: ");
      String to = scanner.nextLine().trim();
       System.out.print("Number of tickets: ");
       int count = readInt();
        if (count <= 0) {
            System.out.println("Ticket count must be positive.");
            return;
        }
       System.out.print("Customer email: ");
      String email = scanner.nextLine().trim();
        int fromIndex = train.indexOfStation(from);
      int toIndex = train.indexOfStation(to);
        if (fromIndex < 0 || toIndex < 0 || fromIndex >= toIndex) {
         System.out.println("Invalid station link on this train.");
            return;
      }
       int free = train.freeSeatsForSegment(fromIndex, toIndex);
        if (free < count) {
         System.out.println("Booking rejected. Not enough free seats. Available: " + free);
         return;
        }
        Booking booking = new Booking(email, from, to, count, LocalDateTime.now());
     train.bookings.add(booking);
        System.out.println("Booking confirmed.");
        System.out.println("Email sent to " + email + ":");
        System.out.println("Your booking is confirmed for train " + train.id + ", " + from + " -> " + to
                + ", tickets: " + count + ".");
    }
    private static void handleFindConnections() {
        System.out.print("Departure station: ");
        String from = scanner.nextLine().trim();
        System.out.print("Arrival station: ");
        String to = scanner.nextLine().trim();
        if (from.equals(to)) {
            System.out.println("Departure and arrival stations are the same.");
            return;
        }
        List<ConnectionOption> options = new ArrayList<>();
        for (Train t : trains.values()) {
            int i = t.indexOfStation(from);
            int j = t.indexOfStation(to);
            if (i >= 0 && j > i) {
                LocalTime start = t.times.get(i);
                LocalTime end = t.times.get(j);
                long minutes = Duration.between(start, end).toMinutes();
                String row = "Direct: Train " + t.id + " | " + from + " " + start.format(timeFormatter)
                        + " -> " + to + " " + end.format(timeFormatter)
                        + " | duration " + formatDuration(minutes);
                options.add(new ConnectionOption(row, minutes));
            }
        }
        for (Train first : trains.values()) {
            int fromIndex = first.indexOfStation(from);
            if (fromIndex < 0) {
                continue;
            }
            for (int midIndex = fromIndex + 1; midIndex < first.stations.size(); midIndex++) {
                String mid = first.stations.get(midIndex);
                LocalTime arriveMid = first.times.get(midIndex);
                for (Train second : trains.values()) {
                    int midSecond = second.indexOfStation(mid);
                    int toSecond = second.indexOfStation(to);
                    if (midSecond >= 0 && toSecond > midSecond) {
                        LocalTime departMid = second.times.get(midSecond);
                        if (!departMid.isBefore(arriveMid)) {
                            LocalTime departStart = first.times.get(fromIndex);
                            LocalTime arriveEnd = second.times.get(toSecond);
                            long minutes = Duration.between(departStart, arriveEnd).toMinutes();
                            String row = "Changeover: Train " + first.id + " " + from + " "
                                    + departStart.format(timeFormatter) + " -> " + mid + " "
                                    + arriveMid.format(timeFormatter) + " | Train " + second.id + " " + mid + " "
                                    + departMid.format(timeFormatter) + " -> " + to + " "
                                    + arriveEnd.format(timeFormatter)
                                    + " | duration " + formatDuration(minutes);
                            options.add(new ConnectionOption(row, minutes));
                        }
                    }
                }
            }
        }
        if (options.isEmpty()) {
            System.out.println("No possible link between these stations.");
            return;
        }
        System.out.println("Possible routes:");
        ConnectionOption best = options.get(0);
        for (ConnectionOption option : options) {
            System.out.println("- " + option.description);
            if (option.totalMinutes < best.totalMinutes) {
                best = option;
            }
        }
        System.out.println("Best option: " + best.description);
    }
    private static void handleAdminMenu() {
        while (true) {
            printAdminMenu();
            String choice = scanner.nextLine().trim();
            if (choice.equals("1")) {
                addRoute();
            } else if (choice.equals("2")) {
                removeRoute();
            } else if (choice.equals("3")) {
                modifyRoute();
            } else if (choice.equals("4")) {
                addTrain();
            } else if (choice.equals("5")) {
                removeTrain();
            } else if (choice.equals("6")) {
                modifyTrain();
            } else if (choice.equals("7")) {
                showBookingsForTrain();
            } else if (choice.equals("8")) {
                registerDelayAndNotify();
            } else if (choice.equals("9")) {
                listRoutesAndTrains();
            } else if (choice.equals("0")) {
                return;
        } else {
                System.out.println("Invalid option.");
            }
            System.out.println();
        }
    }
    private static void printAdminMenu() {
        System.out.println("==== Administrator ====");
        System.out.println("1. Add route");
        System.out.println("2. Remove route");
        System.out.println("3. Modify route stations");
        System.out.println("4. Add train");
        System.out.println("5. Remove train");
        System.out.println("6. Modify train");
        System.out.println("7. Show bookings for train");
        System.out.println("8. Register delay and notify customers");
        System.out.println("9. Show routes and trains");
        System.out.println("0. Back");
        System.out.print("Choose option: ");
    }
    private static void addRoute() {
        System.out.print("New route ID: ");
        String id = scanner.nextLine().trim();
        if (id.isBlank() || routes.containsKey(id)) {
            System.out.println("Invalid or existing route ID.");
            return;
        }
        List<String> stations = readStationsLine();
        if (stations.size() < 2) {
            System.out.println("A route must have at least 2 stations.");
            return;
        }
        routes.put(id, new Route(id, stations));
        System.out.println("Route added.");
    }
    private static void removeRoute() {
        System.out.print("Route ID to remove: ");
        String id = scanner.nextLine().trim();
        if (!routes.containsKey(id)) {
            System.out.println("Route not found.");
            return;
        }
        List<String> linkedTrains = new ArrayList<>();
        for (Train t : trains.values()) {
            if (t.routeId.equals(id)) {
                linkedTrains.add(t.id);
            }
        }
        if (!linkedTrains.isEmpty()) {
            System.out.println("Cannot remove route. Trains still use it: " + linkedTrains);
            return;
        }
        routes.remove(id);
        System.out.println("Route removed.");
    }
    private static void modifyRoute() {
        System.out.print("Route ID to modify: ");
        String id = scanner.nextLine().trim();
        Route route = routes.get(id);
        if (route == null) {
            System.out.println("Route not found.");
            return;
        }
        List<String> stations = readStationsLine();
        if (stations.size() < 2) {
            System.out.println("A route must have at least 2 stations.");
            return;
        }
  route.stations = stations;
        for (Train t : trains.values()) {
            if (t.routeId.equals(id)) {
                t.stations = new ArrayList<>(stations);
                t.times = buildDefaultTimes(stations.size(), LocalTime.of(8, 0));
                t.bookings.clear();
            }
        }
        System.out.println("Route modified. Linked trains were updated with default times and cleared bookings.");
    }
    private static void addTrain() {
        System.out.print("New train ID: ");
        String trainId = scanner.nextLine().trim();
        if (trainId.isBlank() || trains.containsKey(trainId)) {
            System.out.println("Invalid or existing train ID.");
            return;
        }
        System.out.print("Route ID for this train: ");
        String routeId = scanner.nextLine().trim();
        Route route = routes.get(routeId);
        if (route == null) {
            System.out.println("Route not found.");
            return;
        }
        System.out.print("Capacity: ");
        int capacity = readInt();
        if (capacity <= 0) {
            System.out.println("Capacity must be positive.");
            return;
        }
        List<LocalTime> times = readTimesForStations(route.stations);
        if (times == null) {
            return;}
        trains.put(trainId, new Train(trainId, routeId, new ArrayList<>(route.stations), times, capacity));
        System.out.println("Train added.");
    }
    private static void removeTrain() {
        System.out.print("Train ID to remove: ");
        String id = scanner.nextLine().trim();
        if (trains.remove(id) == null) {
            System.out.println("Train not found.");
            return;
        }
        System.out.println("Train removed.");
    }
    private static void modifyTrain() {
     System.out.print("Train ID to modify: ");
        String id = scanner.nextLine().trim();
        Train train = trains.get(id);
      if (train == null) {
         System.out.println("Train not found.");
           return;
        }
        System.out.print("New capacity (current " + train.capacity + "): ");
        int capacity = readInt();
        if (capacity <= 0) {
            System.out.println("Capacity must be positive.");
            return;
        }
      System.out.println("Enter full time list for stations:");
        List<LocalTime> times = readTimesForStations(train.stations);
        if (times == null) {
            return;
      }
        train.capacity = capacity;
       train.times = times;
        System.out.println("Train modified.");
    }
    private static void showBookingsForTrain() {
        System.out.print("Train ID: ");
        String id = scanner.nextLine().trim();
        Train train = trains.get(id);
     if (train == null) {
            System.out.println("Train not found.");
            return;
       }
     if (train.bookings.isEmpty()) {
            System.out.println("No bookings for this train.");
            return;
      }
       System.out.println("Bookings for train " + id + ":");
        for (Booking b : train.bookings) {
           System.out.println("- " + b.email + " | " + b.from + " -> " + b.to + " | tickets: " + b.tickets
                    + " | at: " + b.createdAt.format(dateTimeFormatter));
        }
    }
    private static void registerDelayAndNotify() {
        System.out.print("Train ID: ");
        String id = scanner.nextLine().trim();
        Train train = trains.get(id);
        if (train == null) {
            System.out.println("Train not found.");
            return;
        }
        System.out.print("Delay in minutes: ");
        int delay = readInt();
        if (delay <= 0) {
            System.out.println("Delay must be positive.");
            return;
        }
        if (train.bookings.isEmpty()) {
            System.out.println("No customers to notify.");
            return;
        }
        System.out.println("Notifications:");
        for (Booking b : train.bookings) {
            System.out.println("Email to " + b.email + ": Train " + id + " has a delay of " + delay + " minutes.");
        }
    }
    private static void listRoutesAndTrains() {
        System.out.println("Routes:");
        for (Route r : routes.values()) {
            System.out.println("- " + r.id + ": " + String.join(" -> ", r.stations));
        }
        System.out.println("Trains:");
        for (Train t : trains.values()) {
            System.out.println("- " + t.id + " | route " + t.routeId + " | capacity " + t.capacity);
            for (int i = 0; i < t.stations.size(); i++) {
                System.out.println("  " + t.stations.get(i) + " " + t.times.get(i).format(timeFormatter));
            }
        }
    }
    private static List<String> readStationsLine() {
        System.out.print("Stations (comma separated): ");
        String raw = scanner.nextLine().trim();
        String[] parts = raw.split(",");
        List<String> stations = new ArrayList<>();
        for (String part : parts) {
            String s = part.trim();
            if (!s.isBlank()) {
                stations.add(s);
            }
        }
        return stations;
    }
    private static List<LocalTime> readTimesForStations(List<String> stations) {
        List<LocalTime> times = new ArrayList<>();
        LocalTime prev = null;
        for (String station : stations) {
            System.out.print("Time for " + station + " (HH:mm): ");
            String raw = scanner.nextLine().trim();
            LocalTime time;
            try {
                time = LocalTime.parse(raw, timeFormatter);
            } catch (Exception e) {
                System.out.println("Invalid time.");
                return null;
            }
            if (prev != null && !time.isAfter(prev)) {
                System.out.println("Each next station time must be later than previous.");
                return null;
            }
            times.add(time);
            prev = time;
        }
        return times;
    }
    private static int readInt() {
        String raw = scanner.nextLine().trim();
        try {
            return Integer.parseInt(raw);
        } catch (Exception e) {
            return -1;
        }
    }
    private static void listTrains() {
        System.out.println("Available trains:");
        for (Train t : trains.values()) {
            System.out.println("- " + t.id + " | route " + t.routeId + " | capacity " + t.capacity);
        }
    }
    private static void printTrainStations(Train train) {
        System.out.println("Stations for train " + train.id + ":");
        for (int i = 0; i < train.stations.size(); i++) {
            System.out.println("- " + train.stations.get(i) + " at " + train.times.get(i).format(timeFormatter));
        }
    }
    private static void seedData() {
        Route r1 = new Route("R1", List.of("Bucharest", "Ploiesti", "Brasov", "Sighisoara", "Cluj"));
        Route r2 = new Route("R2", List.of("Constanta", "Bucharest", "Pitesti", "Craiova"));
        Route r3 = new Route("R3", List.of("Iasi", "Bacau", "Brasov", "Sibiu", "Timisoara"));
        routes.put(r1.id, r1);
        routes.put(r2.id, r2);
        routes.put(r3.id, r3);
        trains.put("T1", new Train("T1", "R1", new ArrayList<>(r1.stations),
                List.of(LocalTime.of(8, 0), LocalTime.of(8, 40), LocalTime.of(9, 30), LocalTime.of(10, 30),
                        LocalTime.of(12, 0)),
                120));
        trains.put("T2", new Train("T2", "R2", new ArrayList<>(r2.stations),
                List.of(LocalTime.of(7, 30), LocalTime.of(10, 0), LocalTime.of(11, 30), LocalTime.of(12, 30)), 80));
        trains.put("T3", new Train("T3", "R3", new ArrayList<>(r3.stations),
                List.of(LocalTime.of(6, 0), LocalTime.of(7, 10), LocalTime.of(9, 50), LocalTime.of(11, 0),
                        LocalTime.of(13, 30)),
                100));
    }
    private static List<LocalTime> buildDefaultTimes(int size, LocalTime start) {
        List<LocalTime> times = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            times.add(start.plusMinutes(i * 60L));
        }
        return times;
    }
    private static String formatDuration(long minutes) {
        long hours = minutes / 60;
        long remaining = minutes % 60;
        if (hours == 0) {
            return remaining + "m";
        }
        if (remaining == 0) {
            return hours + "h";
        }
        return hours + "h " + remaining + "m";
    }

    private static class Route {
        private final String id;
        private List<String> stations;

        private Route(String id, List<String> stations) {
            this.id = id;
            this.stations = new ArrayList<>(stations);
        }
    }

    private static class Train {
        private final String id;
        private final String routeId;
        private List<String> stations;
        private List<LocalTime> times;
        private int capacity;
        private final List<Booking> bookings = new ArrayList<>();

        private Train(String id, String routeId, List<String> stations, List<LocalTime> times, int capacity) {
            this.id = id;
            this.routeId = routeId;
            this.stations = stations;
            this.times = new ArrayList<>(times);
            this.capacity = capacity;
        }

        private int indexOfStation(String station) {
            for (int i = 0; i < stations.size(); i++) {
                if (stations.get(i).equalsIgnoreCase(station)) {
                    return i;
                }
            }
            return -1;
        }

        private int freeSeatsForSegment(int fromIndex, int toIndex) {
            int maxUsed = 0;
            for (int seg = fromIndex; seg < toIndex; seg++) {
                int used = 0;
                for (Booking b : bookings) {
                    int bFrom = indexOfStation(b.from);
                    int bTo = indexOfStation(b.to);
                    if (bFrom <= seg && bTo > seg) {
                        used += b.tickets;
                    }
                }
                if (used > maxUsed) {
                    maxUsed = used;
                }
            }
            return capacity - maxUsed;
        }
    }
    private static class Booking {
        private final String email;
        private final String from;
        private final String to;
        private final int tickets;
        private final LocalDateTime createdAt;
        private Booking(String email, String from, String to, int tickets, LocalDateTime createdAt) {
            this.email = email;
            this.from = from;
            this.to = to;
            this.tickets = tickets;
            this.createdAt = createdAt;
        }
    }
    private static class ConnectionOption {
        private final String description;
        private final long totalMinutes;
        private ConnectionOption(String description, long totalMinutes) {
            this.description = description;
            this.totalMinutes = totalMinutes;
        }
    }
}
