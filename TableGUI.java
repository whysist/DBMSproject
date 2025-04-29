import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

public class TableGUI {
    private static final String DB_URL = "* ";
    private static final String user = "*";
    private static final String pass = "*";

    private JTextField rollField, nameField, courseField, sectionField, statusField, marksField, durationField, photocopy;
    private JButton createButton, deleteButton, editButton;
    private JTable studentTable;
    private DefaultTableModel model;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new TableGUI().createGUI());
    }

    public void createGUI(){
        JFrame frame = new JFrame("NPTEL Database");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 500);
        frame.setLayout(new BorderLayout());

        JPanel tablePanel = new JPanel();
        tablePanel.setLayout(new BorderLayout());

        model = new DefaultTableModel(new String[]{"Roll No", "Name", "Course", "Duration", "Section", "Status", "Marks","Grade"}, 0);
        studentTable = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(studentTable);
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model); //sorter apparently
        studentTable.setRowSorter(sorter); //connecting sorter to the table
        studentTable.setAutoCreateRowSorter(true);//automatically sorts when clicked on the column

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        createButton = new JButton("Insert");
        createButton.addActionListener(e -> openInsertWindow());
        buttonPanel.add(createButton);

        deleteButton = new JButton("Delete");
        deleteButton.addActionListener(e -> openDeleteWindow());
        buttonPanel.add(deleteButton);

        editButton = new JButton("Update");
        editButton.addActionListener(e -> openUpdateWindow());
        buttonPanel.add(editButton);

        frame.add(tablePanel, BorderLayout.CENTER);
        frame.add(buttonPanel, BorderLayout.EAST);
        frame.setVisible(true);
        try {
            loadData(); //loads tee data from db
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
private void openInsertWindow() {
    JDialog insertDialog = new JDialog();
    insertDialog.setTitle("Insert Student");
    insertDialog.setSize(400, 400);
    insertDialog.setLayout(new GridLayout(4,2,10,10));

    insertDialog.add(new JLabel("Roll No:"));
    rollField = new JTextField(30);
    insertDialog.add(rollField);

    insertDialog.add(new JLabel("Name:"));
    nameField = new JTextField(30);
    //nameField.setSize(200,30);
    insertDialog.add(nameField);

    insertDialog.add(new JLabel("Course:"));
    courseField = new JTextField(30);
    //courseField.setSize(200,30);
    insertDialog.add(courseField);

    insertDialog.add(new JLabel("Duration:"));
    durationField = new JTextField(30);
    //durationField.setSize(200,30);
    insertDialog.add(durationField);

    insertDialog.add(new JLabel("Section:"));
    sectionField = new JTextField(30);
  //  sectionField.setSize(200,30);
    insertDialog.add(sectionField);

    insertDialog.add(new JLabel("Status:"));
    statusField = new JTextField(30);
    //statusField.setSize(200,30);
    insertDialog.add(statusField);

    insertDialog.add(new JLabel("Marks:"));
    marksField = new JTextField(30);
    //marksField.setSize(200,30);
    insertDialog.add(marksField);

    insertDialog.add(new JLabel("Photocopy:"));
    photocopy = new JTextField(30);
    //photocopy.setSize(200,30);
    insertDialog.add(photocopy);

    JButton insertButton = new JButton("Insert");
    insertButton.addActionListener(e -> createRecord());
    insertDialog.add(insertButton);

    insertDialog.setVisible(true);
    //loadData();
    }
    private void createRecord(){
        String rollno=rollField.getText().trim();
        String name = nameField.getText().trim();
        String course = courseField.getText().trim();
        String section = sectionField.getText().trim();
        String durationText=durationField.getText().trim();
        String pht_sub=photocopy.getText().trim();
        int duration=Integer.parseInt(durationText);
        String status = statusField.getText().trim();

        int marks = 0;
        String marksText = marksField.getText().trim();
        if (!marksText.isEmpty()) {
            marks = Integer.parseInt(marksText);
        }

        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = DriverManager.getConnection(DB_URL, user, pass);
            String checkCourseSql = "SELECT course_id FROM whysist.COURSE WHERE course_name = ?";
            stmt = conn.prepareStatement(checkCourseSql);
            stmt.setString(1, course);
            ResultSet rs = stmt.executeQuery();
            int newCourseId;

            if (rs.next()) {
                newCourseId = rs.getInt("course_id");
            } else {
                // Course does not exist, get the max course_id and insert the new course
                String getMaxCourseIdSql = "SELECT MAX(course_id) AS max_course_id FROM whysist.course";
                stmt = conn.prepareStatement(getMaxCourseIdSql);
                ResultSet rs1 = stmt.executeQuery();
                if (rs1.next()) {
                    newCourseId = rs1.getInt("max_course_id") + 1;
                } else {
                    newCourseId = 1; // If there are no courses, start from course_id 1
                }
                

            String insertSql= "INSERT INTO whysist.COURSE(course_name,duration,course_id) VALUES(?,?,?)";
            stmt=conn.prepareStatement(insertSql,Statement.RETURN_GENERATED_KEYS);
            stmt.setString(1,course);
            stmt.setString(2, String.valueOf(duration));
            stmt.setString(3, String.valueOf(newCourseId));
            stmt.executeUpdate();
            }

            String insertSqlStud="INSERT INTO whysist.student(roll_no,name,section,semester) VALUES (?,?,?,?)";
            stmt=conn.prepareStatement(insertSqlStud);
            stmt.setString(1,rollno);
            stmt.setString(2,name);
            stmt.setString(3,section);
            stmt.setString(4,"VIII");
            stmt.executeUpdate();

            String insertSqlenrol= "INSERT INTO whysist.enrolls(roll_no,course_id,marks,status,photocopy_sub) VALUES(?,?,?,?,?)";
            stmt=conn.prepareStatement(insertSqlenrol);
            stmt.setString(1,rollno);
            stmt.setString(2, String.valueOf(newCourseId));
            stmt.setString(3, String.valueOf(marks));
            stmt.setString(4,status);
            stmt.setString(5,pht_sub);
            stmt.executeUpdate();

            loadData();

        } catch (SQLException e) {
            e.printStackTrace();
        } finally
        {
            try {
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

    }
    private void openDeleteWindow() {
        JDialog deleteDialog = new JDialog();
        deleteDialog.setTitle("Delete Student");
        deleteDialog.setSize(300, 200);
        deleteDialog.setLayout(new BorderLayout());

        JLabel deleteLabel = new JLabel("Enter Roll No to Delete:");
        JTextField RollField = new JTextField();
        JButton deleteButton = new JButton("Delete");
        deleteButton.addActionListener(e -> deleteRecord(RollField.getText()));
        deleteDialog.add(deleteLabel, BorderLayout.NORTH);
        deleteDialog.add(RollField, BorderLayout.CENTER);
        deleteDialog.add(deleteButton, BorderLayout.SOUTH);
        deleteDialog.setVisible(true);
        //loadData();

    }
    private void deleteRecord(String rollNo){
        try (Connection conn = DriverManager.getConnection(DB_URL, user, pass)){
            conn.setAutoCommit(false);
            try {
                String sql = "DELETE FROM whysist.enrolls WHERE roll_no = ?";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, rollNo);
                stmt.executeUpdate(); //Deleting from enrolls table first (foreign key kada)

            String deleteSql="DELETE FROM whysist.student WHERE roll_no=?";
                PreparedStatement stmt2 = conn.prepareStatement(deleteSql);
                stmt2.setString(1, rollNo);
                stmt2.executeUpdate();
            } catch (SQLException e) {
                conn.rollback();
                e.printStackTrace();
                throw new RuntimeException(e);
            }
            conn.commit();
            loadData();
            } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    private void openUpdateWindow(){
        JDialog updateDialog = new JDialog();
        updateDialog.setTitle("Update Student");
        updateDialog.setSize(400, 400);
        updateDialog.setLayout(new GridLayout(4, 2, 10, 10));

        updateDialog.add(new JLabel("Roll No:"));
        rollField = new JTextField(30);
        updateDialog.add(rollField);

        updateDialog.add(new JLabel("Name:"));
        nameField = new JTextField(30);
        updateDialog.add(nameField);

        updateDialog.add(new JLabel("Course:"));
        courseField = new JTextField(30);
        updateDialog.add(courseField);

        updateDialog.add(new JLabel("Duration:"));
        durationField = new JTextField(30);
        updateDialog.add(durationField);

        updateDialog.add(new JLabel("Section:"));
        sectionField = new JTextField(30);
        updateDialog.add(sectionField);

        updateDialog.add(new JLabel("Status:"));
        statusField = new JTextField(30);
        updateDialog.add(statusField);

        updateDialog.add(new JLabel("Marks:"));
        marksField = new JTextField(30);
        updateDialog.add(marksField);

        updateDialog.add(new JLabel("Photocopy:"));
        photocopy = new JTextField(30);
        updateDialog.add(photocopy);

        JButton updateButton = new JButton("Update");
        updateButton.addActionListener(e -> updateRecord());
        updateDialog.add(updateButton);

        updateDialog.setVisible(true);
    }
    private void updateRecord(){
        String rollno = rollField.getText().trim();
        String name = nameField.getText().trim();
        String course = courseField.getText().trim();
        String section = sectionField.getText().trim();
        String durationText = durationField.getText().trim();
        String pht_sub = photocopy.getText().trim();
        int duration = Integer.parseInt(durationText);
        String status = statusField.getText().trim();
        int marks = Integer.parseInt(marksField.getText().trim());
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = DriverManager.getConnection(DB_URL, user, pass);
            String checkCourseSql = "SELECT course_id FROM whysist.COURSE WHERE course_name = ?";
            stmt = conn.prepareStatement(checkCourseSql);
            stmt.setString(1, course);
            ResultSet rs = stmt.executeQuery();
            int newCourseId = -1;

            if (rs.next()) {
                newCourseId = rs.getInt("course_id");
            } else {
                // If the course does not exist, create a new one
                String getMaxCourseIdSql = "SELECT MAX(course_id) AS max_course_id FROM whysist.course";
                stmt = conn.prepareStatement(getMaxCourseIdSql);
                rs = stmt.executeQuery();
                if (rs.next()) {
                    newCourseId = rs.getInt("max_course_id") + 1;
                }
                // Insert the new course
                String insertCourseSql = "INSERT INTO whysist.COURSE(course_name, duration, course_id) VALUES(?, ?, ?)";
                stmt = conn.prepareStatement(insertCourseSql);
                stmt.setString(1, course);
                stmt.setInt(2, duration);
                stmt.setInt(3, newCourseId);
                stmt.executeUpdate();
            }

            // Step 2: Update the student record if rollno exists
            String updateStudentSql = "UPDATE whysist.student SET name = ?, section = ?, semester = 'VIII' WHERE roll_no = ?";
            stmt = conn.prepareStatement(updateStudentSql);
            stmt.setString(1, name);
            stmt.setString(2, section);
            stmt.setString(3, rollno);
            int updatedRows = stmt.executeUpdate();

            // If no rows were updated, the student doesn't exist, handle accordingly
            if (updatedRows == 0) {
                JOptionPane.showMessageDialog(null, "Student with Roll No: " + rollno + " does not exist.");
                return;
            }

            // Step 3: Update the enrollment record
            String updateEnrollSql = "UPDATE whysist.enrolls SET marks = ?, status = ?, photocopy_sub = ? WHERE roll_no = ? AND course_id = ?";
            stmt = conn.prepareStatement(updateEnrollSql);
            stmt.setInt(1, marks);
            stmt.setString(2, status);
            stmt.setString(3, pht_sub);
            stmt.setString(4, rollno);
            stmt.setInt(5, newCourseId);
            stmt.executeUpdate();

            // Refresh the data in the table after the update
            loadData();

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    private void loadData() throws SQLException {
        model.setRowCount(0);//clears the table
        String sql="SELECT s.roll_no,s.name,c.course_name,c.duration,s.section,e.status,e.marks FROM whysist.student s JOIN whysist.enrolls e ON s.roll_no=e.roll_no JOIN whysist.course c ON e.course_id=c.course_id";
        try(Connection conn = DriverManager.getConnection(DB_URL, user, pass);
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery()){

            while(rs.next()){
                String rollNo = rs.getString("roll_no");
                String name = rs.getString("name");
                String course = rs.getString("course_name");
                String duration = rs.getString("duration");
                String section = rs.getString("section");
                String status = rs.getString("status");
                int marks = rs.getInt("marks");
                String grade= calculateGrade(marks);
                model.addRow(new Object[]{rollNo, name, course, duration, section, status, marks,grade});
            }
        }catch (SQLException e){
            e.printStackTrace();
        }
    }
    private String calculateGrade(int marks) {
        if (marks == 0) {
            return "NA";
        }
        try {
            if (marks >= 90) {
                return "Elite + Gold Medal";
            } else if (marks >= 60) {
                return "Elite";
            } else if (marks >= 40) {
                return "Successfully Completed";
            } else {
                return "Failed";  // Optionally, handle the case for marks below 40
            }
        } catch (NumberFormatException e) {
            return "Invalid Marks";
        }
    }

}