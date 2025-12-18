import javax.xml.transform.Result;
import java.util.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.io.FileInputStream;
import java.util.Properties;
import java.sql.*;
class DAO{
    public class DBConnection {

        private static String url;
        private static String username;
        private static String password;

        static {
            try {
                Properties props = new Properties();
                props.load(new FileInputStream("src/resources/config.properties"));

                url = props.getProperty("db.url");
                username = props.getProperty("db.username");
                password = props.getProperty("db.password");

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public static Connection getConnection() throws SQLException {
            return DriverManager.getConnection(url, username, password);
        }
    }

    public void createHotelDB(){
        String query = "Create database if not exists hotel_db";
        try(Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(query);){
            ps.executeUpdate();
            System.out.println("Database successfully created or exist");
        }catch(SQLException e){
            System.out.println(e.getMessage());
        }
    }
    public void createRoomsWeHaveTable() {
        String query = "Create table if not exists roomswehave(" +
                "room_number char(3) primary key," +
                "reservation_id int default null," +
                "status enum('full','empty') not null default 'empty'," +
                "CONSTRAINT chk_room_range CHECK (" +
                "    room_number BETWEEN '001' AND '020'" +
                "    OR room_number BETWEEN '101' AND '120'" +
                "    OR room_number BETWEEN '201' AND '220'" +
                ")"+
                ")";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(query);) {
            ps.executeUpdate();
            System.out.println("Table Created");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        query = "INSERT IGNORE INTO roomswehave (room_number) VALUES" +
                "('001'), ('002'), ('003'), ('004'), ('005')," +
                "('006'), ('007'), ('008'), ('009'), ('010')," +
                "('011'), ('012'), ('013'), ('014'), ('015')," +
                "('016'), ('017'), ('018'), ('019'), ('020')," +
                "('101'), ('102'), ('103'), ('104'), ('105')," +
                "('106'), ('107'), ('108'), ('109'), ('110')," +
                "('111'), ('112'), ('113'), ('114'), ('115')," +
                "('116'), ('117'), ('118'), ('119'), ('120')," +
                "('201'), ('202'), ('203'), ('204'), ('205')," +
                "('206'), ('207'), ('208'), ('209'), ('210')," +
                "('211'), ('212'), ('213'), ('214'), ('215')," +
                "('216'), ('217'), ('218'), ('219'), ('220')";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(query);) {
            ps.executeUpdate();
            System.out.println("Data Inserted");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
    public void createReservationTable(){
        String query = "Create table if not exists reservation(" +
                "reservation_id int auto_increment primary key," +
                "guest_name varchar(50) not null," +
                "room_number char(3) not null," +
                "contact_number varchar(50) not null," +
                "check_in date default (current_date)," +
                "check_out date not null," +
                "total_bill int not null," +
                "constraint fk_room foreign key(room_number) " +
                "references roomswehave(room_number) " +
                ")";
        try(Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(query);){
            ps.executeUpdate();
            System.out.println("Table Created");
        }catch(SQLException e){
            System.out.println(e.getMessage());
        }
    }
    public void showAvailableRooms() {
        System.out.println("Showing Available Rooms:");
        String query = "Select room_number from roomswehave where status = 'empty'";
        try(Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(query);
            ResultSet rs = ps.executeQuery();) {
            while(rs.next()){
                System.out.print(rs.getString("room_number")+" ");
            }
            System.out.println();
        }catch (SQLException e){
            System.out.println(e.getMessage());
        }
    }
    public void newReservation(Scanner sc){
        System.out.println("\nMaking New Reservation");
        System.out.println("================================================");
        System.out.println("Please Enter Following Details:");
        System.out.print("Name: ");
        String name = sc.nextLine();
        System.out.println("This are the available rooms: ");
        showAvailableRooms();
        System.out.println();
        System.out.print("Enter Room number: ");
        String room_no = sc.nextLine();
        System.out.print("Enter Contact No. like(1234567890) : ");
        String contact = sc.nextLine();
        System.out.println("Enter check-in date (yyyy-MM-dd) or type default for today's date");
        String checkIn = sc.nextLine();
        LocalDate checkInDate;
        if(checkIn.equalsIgnoreCase("DEFAULT")){
            checkInDate = LocalDate.now();
        }else{
            try {
                checkInDate = LocalDate.parse(checkIn);
                System.out.println("Check-in date accepted: " + checkIn);
            } catch (DateTimeParseException e) {
                System.out.println("❌ Invalid date format. Use yyyy-MM-dd");
                return;
            }
        }
        System.out.print("Enter check-out date (yyyy-MM-dd): ");
        String checkOut = sc.nextLine();
        LocalDate checkOutDate;
        try {
            checkOutDate = LocalDate.parse(checkOut);
            System.out.println("Check-out date accepted: " + checkOut);
        } catch (DateTimeParseException e) {
            System.out.println("❌ Invalid date format. Use yyyy-MM-dd");
            return;
        }
        String checkQuery = "Select status from roomswehave where room_number = ?";
        String insertReservation = "INSERT INTO reservation (guest_name, room_number, contact_number,check_in, check_out, total_bill) VALUES (?, ?, ?, ?, ?, ?)";
        String updateRoom = "UPDATE roomswehave SET status = 'FULL', reservation_id = ? WHERE room_number = ?";
        try (Connection con = DBConnection.getConnection()) {

            // 🔒 Start transaction
            con.setAutoCommit(false);

            // 1️⃣ Check room status
            PreparedStatement psCheck = con.prepareStatement(checkQuery);
            psCheck.setString(1, room_no);

            ResultSet rs = psCheck.executeQuery();

            if (!rs.next()) {
                System.out.println("❌ Invalid room number");
                con.rollback();
                return;
            }

            if (!rs.getString("status").equalsIgnoreCase("EMPTY")) {
                System.out.println("❌ Room already FULL");
                con.rollback();
                return;
            }

            // 2️⃣ Insert reservation
            PreparedStatement psInsert =
                    con.prepareStatement(insertReservation, Statement.RETURN_GENERATED_KEYS);

            psInsert.setString(1, name);
            psInsert.setString(2, room_no);
            psInsert.setString(3, contact);
            psInsert.setDate(4,java.sql.Date.valueOf(checkInDate));
            psInsert.setDate(5,java.sql.Date.valueOf(checkOutDate));
            int days = (int) ChronoUnit.DAYS.between(checkInDate, checkOutDate);
            int bill = days*1500;
            psInsert.setInt(6,bill);
            psInsert.executeUpdate();

            ResultSet keys = psInsert.getGeneratedKeys();
            keys.next();
            int reservationId = keys.getInt(1);

            // 3️⃣ Update room
            PreparedStatement psUpdate = con.prepareStatement(updateRoom);
            psUpdate.setInt(1, reservationId);
            psUpdate.setString(2, room_no);
            psUpdate.executeUpdate();

            // ✅ Commit
            con.commit();
            System.out.println("✅ Room successfully reserved");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public void checkReservation(Scanner sc){
        System.out.println("Check by:");
        System.out.println("1. Name");
        System.out.println("2. Room Number");
        System.out.println("3. Contact No.");
        System.out.println("4. Reservation id");
        int choice = sc.nextInt();
        sc.nextLine();
        String query = null;
        try (Connection con = DBConnection.getConnection()) {

            PreparedStatement ps = null;

            switch (choice) {

                case 1:
                    System.out.print("Enter Name: ");
                    String name = sc.nextLine();
                    query = "SELECT * FROM reservation WHERE guest_name = ?";
                    ps = con.prepareStatement(query);
                    ps.setString(1, name);
                    break;

                case 2:
                    System.out.print("Enter Room No.: ");
                    String roomNo = sc.nextLine();
                    query = "SELECT * FROM reservation WHERE room_number = ?";
                    ps = con.prepareStatement(query);
                    ps.setString(1, roomNo);
                    break;

                case 3:
                    System.out.print("Enter Contact Number: ");
                    String contact = sc.nextLine();
                    query = "SELECT * FROM reservation WHERE contact_number = ?";
                    ps = con.prepareStatement(query);
                    ps.setString(1, contact);
                    break;

                case 4:
                    System.out.print("Enter Reservation id: ");
                    int resId = sc.nextInt();
                    query = "SELECT * FROM reservation WHERE reservation_id = ?";
                    ps = con.prepareStatement(query);
                    ps.setInt(1, resId);
                    break;

                default:
                    System.out.println("Invalid option");
                    return;
            }

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    System.out.println(
                            rs.getInt("reservation_id") + " | " +
                                    rs.getString("guest_name") + " | " +
                                    rs.getString("room_number") + " | " +
                                    rs.getString("contact_number") + " | " +
                                    rs.getDate("check_in") + " | " +
                                    rs.getDate("check_out") + " | " +
                                    rs.getInt("total_bill")
                    );
                } else {
                    System.out.println("No reservation found");
                }
            }

        } catch (SQLException e) {
            System.out.println("DB Error: " + e.getMessage());
        }
    }
    public void updateReservation(Scanner sc){
        System.out.println("Updation wanted???");
        System.out.println("Select what you wanna change and make sure you should have a Reservation id");
        System.out.println("1. Name");
        System.out.println("2. Room No.");
        System.out.println("3. Contact Info");
        System.out.println("4. Check_In Date");
        System.out.println("5. Check_Out Date");
        int choice = sc.nextInt();
        sc.nextLine();
        System.out.print("Enter Reservation id: ");
        int res_id = sc.nextInt();
        sc.nextLine();
        String check_reservation = "Select 1 from reservation where reservation_id = ?";
        try(Connection con = DBConnection.getConnection()){
            con.setAutoCommit(false);
            try(PreparedStatement checkPs = con.prepareStatement(check_reservation)){
                checkPs.setInt(1,res_id);
                try (ResultSet rs = checkPs.executeQuery()) {

                    if (!rs.next()) {
                        System.out.println("❌ Invalid Reservation ID");
                        return;
                    }
                }
            }
            switch (choice) {
                case 1:
                    System.out.print("Enter new name: ");
                    String name = sc.nextLine();
                    String updateName = "Update reservation set guest_name = ? where reservation_id = ?";
                    try(PreparedStatement ps = con.prepareStatement(updateName)){
                        ps.setString(1, name);
                        ps.setInt(2, res_id);
                        ps.executeUpdate();
                    }
                    System.out.println("✅ Name updated successfully");
                    break;

                case 2:
                    System.out.print("Enter new room number like '001' or '112'");
                    String room_no = sc.nextLine();
                    String checkRoom = "SELECT status from roomswehave where room_number = ?";
                    try(PreparedStatement ps = con.prepareStatement(checkRoom)){
                        ps.setString(1,room_no);
                        try(ResultSet rs = ps.executeQuery()){
                            if (!rs.next()) {
                                System.out.println("❌ Invalid room number");
                                con.rollback();
                                return;
                            }

                            if (!rs.getString("status")
                                    .equalsIgnoreCase("EMPTY")) {
                                System.out.println("❌ Room already FULL");
                                con.rollback();
                                return;
                            }
                        }
                    }
                    try(PreparedStatement ps = con.prepareStatement("UPDATE roomswehave SET status='empty',reservation_id = null WHERE reservation_id = ?")){
                        ps.setInt(1,res_id);
                        ps.executeUpdate();
                    }
                    try (PreparedStatement ps = con.prepareStatement("UPDATE roomswehave SET status='FULL', reservation_id=? WHERE room_number=?")) {
                        ps.setInt(1, res_id);
                        ps.setString(2, room_no);
                        ps.executeUpdate();
                    }
                    try(PreparedStatement ps = con.prepareStatement("update reservation set room_number = ? where reservation_id = ?")){
                        ps.setString(1,room_no);
                        ps.setInt(2,res_id);
                        ps.executeUpdate();
                    }

                    System.out.println("✅ Room updated successfully");
                    break;
                case 3:
                    System.out.print("Enter new phone number");
                    String phone_no = sc.nextLine();
                    String update_phoneno = "update reservation set contact_number = ? where reservation_id = ?";
                    try(PreparedStatement ps = con.prepareStatement(update_phoneno)){
                        ps.setString(1,phone_no);
                        ps.setInt(2,res_id);
                        ps.executeUpdate();
                    }
                    System.out.println("✅ Phone number updated successfully");
                    break;

                case 4:
                    System.out.println("Enter new Check_In date like 2025-01-01");
                    String checkIn = sc.nextLine();
                    LocalDate today = LocalDate.now();
                    LocalDate checkInDate;
                    LocalDate checkOutDate;
                    try {
                        checkInDate = LocalDate.parse(checkIn);
                        System.out.println("Check-in date accepted: " + checkIn);
                    } catch (DateTimeParseException e) {
                        System.out.println("❌ Invalid date format. Use yyyy-MM-dd");
                        return;
                    }
                    String getCheckoutDate = "select check_out from reservation where reservation_id = ?";
                    try(PreparedStatement ps =con.prepareStatement(getCheckoutDate)){
                        ps.setInt(1,res_id);
                        try(ResultSet rs = ps.executeQuery()){
                            if(!rs.next()){
                                System.out.println("Reservation not found!!!");
                                return;
                            }
                            checkOutDate = rs.getDate("check_out").toLocalDate();
                        }
                    }
                    if((today.isBefore(checkInDate) || today.isEqual(checkInDate))&& checkInDate.isBefore(checkOutDate)){
                        String checkInquery = "update reservation set check_in = ?,total_bill = ? where reservation_id = ?";
                        try(PreparedStatement ps = con.prepareStatement(checkInquery)){
                            ps.setDate(1,java.sql.Date.valueOf(checkInDate));
                            int days = (int) ChronoUnit.DAYS.between(checkInDate, checkOutDate);
                            if(days<=0){
                                System.out.println("Invalid reservation duration");
                                return;
                            }
                            int bill = days*1500;
                            ps.setInt(2,bill);
                            ps.setInt(3,res_id);
                            ps.executeUpdate();
                        }
                        System.out.println("✅ Check In updated successfully");
                    }else{
                        System.out.println("Invalid check_in date");
                        return;
                    }
                    break;

                case 5:
                    System.out.println("Enter new Check_Out date like 2025-12-01");
                    String checkOut = sc.nextLine();
                    LocalDate todays = LocalDate.now();
                    LocalDate checkInDates;
                    LocalDate checkOutDates;
                    try {
                        checkOutDates = LocalDate.parse(checkOut);
                        System.out.println("Check-out date accepted: " + checkOut);
                    } catch (DateTimeParseException e) {
                        System.out.println("❌ Invalid date format. Use yyyy-MM-dd");
                        return;
                    }
                    String getCheckinDate = "select check_in from reservation where reservation_id = ?";
                    try(PreparedStatement ps =con.prepareStatement(getCheckinDate)){
                        ps.setInt(1,res_id);
                        try(ResultSet rs = ps.executeQuery()){
                            if(!rs.next()){
                                System.out.println("Reservation not found!!!");
                                return;
                            }
                            checkInDates = rs.getDate("check_in").toLocalDate();
                        }
                    }
                    if (checkOutDates.isAfter(checkInDates) && checkOutDates.isAfter(todays)){
                        String checkOutquery = "update reservation set check_out = ?,total_bill = ? where reservation_id = ?";
                        try(PreparedStatement ps = con.prepareStatement(checkOutquery)){
                            ps.setDate(1,java.sql.Date.valueOf(checkOutDates));
                            int days = (int) ChronoUnit.DAYS.between(checkInDates, checkOutDates);
                            if(days<=0){
                                System.out.println("Invalid reservation duration");
                                return;
                            }
                            int bill = days*1500;
                            ps.setInt(2,bill);
                            ps.setInt(3,res_id);
                            ps.executeUpdate();
                        }
                        System.out.println("✅ Check Out updated successfully");
                    }else{
                        System.out.println("Invalid check_out date");
                        return;
                    }
                    break;

                default:
                    System.out.println("❌ Invalid choice");
                    con.rollback();
                    return;
            }
            con.commit();
            System.out.println("🎉 Transaction committed");
            String updation_result = "select * from reservation where reservation_id = ?";
            try(Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(updation_result);){
                ps.setInt(1,res_id);
                try(ResultSet rs = ps.executeQuery()){
                    if (!rs.next()) {
                        System.out.println("No reservation found");
                        return;
                    }
                    do {
                        System.out.println(
                                rs.getInt("reservation_id") + " | " +
                                        rs.getString("guest_name") + " | " +
                                        rs.getString("room_number") + " | " +
                                        rs.getString("contact_number") + " | " +
                                        rs.getDate("check_in") + " | " +
                                        rs.getDate("check_out") + " | " +
                                        rs.getInt("total_bill")
                        );
                    } while (rs.next());
                }
            }catch(SQLException e){
                e.printStackTrace();
            }
        }catch(SQLException e){
            System.out.println(e.getMessage());
        }
    }
    public void deleteReservation(Scanner sc){
        System.out.println("Deletion want??");
        System.out.println("Delete by only your reservation id only possible!!");
        int res_id = sc.nextInt();
        sc.nextLine();
        int bill = 0;
        String getbill = "select total_bill from reservation where reservation_id = ?";
        try(Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(getbill);){
            ps.setInt(1,res_id);
            try(ResultSet rs = ps.executeQuery()){
                if (!rs.next()) {
                    System.out.println("❌ Invalid reservatio id");
                    return;
                }
                if(rs.next()){
                    bill = rs.getInt("total_bill");
                }
                System.out.println("Your total bill is: "+bill);
            }catch (SQLException e){
                e.printStackTrace();
            }
        }catch(SQLException e){
            e.printStackTrace();
        }
        String deletion_query = "delete from reservation where reservation_id = ?";
        String updation_query = "update roomswehave set reservation_id = ?, status = ? where reservation_id = ?";
        try(Connection con = DBConnection.getConnection()){
            con.setAutoCommit(false);
            PreparedStatement ps = con.prepareStatement(deletion_query);
            ps.setInt(1,res_id);
            ps.executeUpdate();
            PreparedStatement updation = con.prepareStatement(updation_query);
            updation.setString(1,null);
            updation.setString(2,"empty");
            updation.setInt(3,res_id);
            updation.executeUpdate();
            con.commit();
            System.out.println("✅ Room successfully empty");
        }catch (SQLException e){
            e.printStackTrace();
        }
    }
    public void seeAll(){
        System.out.println("Listing all the data");
        String reservation_query = "Select * from reservation";
        String rooms_query = "Select * from roomswehave";
        try(Connection con = DBConnection.getConnection()){
            PreparedStatement ps = con.prepareStatement(reservation_query);
            try(ResultSet rs = ps.executeQuery()){
                if(!rs.next()){
                    System.out.println("No Data to show in reservation");
                    return;
                }
                do {
                    System.out.println(
                            rs.getInt("reservation_id") + " | " +
                                    rs.getString("guest_name") + " | " +
                                    rs.getString("room_number") + " | " +
                                    rs.getString("contact_number") + " | " +
                                    rs.getDate("check_in") + " | " +
                                    rs.getDate("check_out") + " | " +
                                    rs.getInt("total_bill")
                    );
                } while (rs.next());
            }catch(SQLException e){
                e.printStackTrace();
            }
            PreparedStatement ps2 = con.prepareStatement(rooms_query);
            try(ResultSet rss = ps2.executeQuery()){
                if(!rss.next()){
                    System.out.println("No Data to show in rooms");
                    return;
                }
                do {
                    System.out.println(
                            rss.getString("room_number") + " | " +
                                    rss.getInt("reservation_id") + " | " +
                                    rss.getString("status")
                    );
                } while (rss.next());
            }catch(SQLException e){
                e.printStackTrace();
            }
        }catch (SQLException e){
            e.printStackTrace();
        }
    }
}
public class HotelReservationSystem {
    public static void main(String[] args){
        Scanner sc = new Scanner(System.in);
        DAO d1 = new DAO();
        System.out.println("=================Welcome To Chat with GPT Restaurant and Hotel===================");
        System.out.println("Initializing Database.....");
//        d1.createHotelDB();
        System.out.println("Creating Required tables and inserting Datas");
        d1.createRoomsWeHaveTable();
        d1.createReservationTable();
        d1.showAvailableRooms();
        int task;
        do{
            System.out.println("Make a Choice: ");
            System.out.println("1. See Available Rooms.");
            System.out.println("2. Make a new reservation.");
            System.out.println("3. Check your reservation with Reservation id.");
            System.out.println("4. Update Reservation.");
            System.out.println("5. Delete reservation.");
            System.out.println("6. See All Data");
            System.out.println("7. Exit");
            task = sc.nextInt();
            sc.nextLine();
            switch (task){
                case 1:
                    d1.showAvailableRooms();
                    break;
                case 2:
                    d1.newReservation(sc);
                    break;
                case 3:
                    d1.checkReservation(sc);
                    break;
                case 4:
                    d1.updateReservation(sc);
                    break;
                case 5:
                    d1.deleteReservation(sc);
                    break;
                case 6:
                    d1.seeAll();
                    break;
                case 7:
                    System.out.println("Exiting....");
                    break;
                default:
                    System.out.println("Incorrect choice Try Again....");
            }

        }while(task!=7);
    }
}
