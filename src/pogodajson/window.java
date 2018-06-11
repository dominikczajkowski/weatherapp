package pogodajson;

import java.awt.BorderLayout;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.Vector;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import org.json.JSONException;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import javax.swing.JTextField;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import javax.swing.JLabel;
import net.miginfocom.swing.MigLayout;
import javax.swing.JTextPane;

public class window extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3867916048207558852L;
	private JPanel contentPane;
	private final JTextField textField = new JTextField();
	private JTextField textField_1;
    private Connection connect = null;
    
    private PreparedStatement preparedStatement = null;
    private ResultSet resultSet = null;
    private JButton btnReadFromDb;
    private JLabel label;
    private JLabel label_1;
    private JTextPane textPane;
    private Vector<String> city;
    private Vector<String> temp;
    private Vector<String> country;
    private JButton btnEmptyDb;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					window frame = new window();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public window() {
		city = new Vector<String>();  
		temp = new Vector<String>(); 
		country = new Vector<String>(); 
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		
		
        // Setup the connection with the DB
        try {
        	try {
				Class.forName("com.mysql.jdbc.Driver");
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			connect = DriverManager
			        .getConnection("jdbc:mysql://localhost/weatherDB?"
			                + "user=nowyUzytkownik&password=666");
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		contentPane.setLayout(new MigLayout("", "[220px,grow][][220px]", "[96px][][96px][96px,grow]"));
		
		textField_1 = new JTextField();
		contentPane.add(textField_1, "cell 0 0,grow");
		textField_1.setColumns(10);
		
		label = new JLabel("");
		contentPane.add(label, "cell 2 0,grow");
		
		JButton btnNewButton = new JButton("Pobierz pogodę");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				try {
					String url = "http://api.openweathermap.org/data/2.5/weather?q=country&mode=json&apikey=ab3a3a4743c325386addd4cfe19adfba";
					String country = textField_1.getText();
					HttpResponse<JsonNode> weather = Unirest
							.get(url.replace("country",country))
							.asJson();
					textField.setText("City: "+weather.getBody().getObject().get("name").toString()+"\n"+
					"Temp: "+weather.getBody().getObject().getJSONObject("main").get("temp").toString()+"\n"+
					"Country: "+weather.getBody().getObject().getJSONObject("sys").get("country").toString());
					preparedStatement = connect
		                    .prepareStatement("insert into  data values (?, ?, ?)");

		            preparedStatement.setString(1, weather.getBody().getObject().get("name").toString());
		            preparedStatement.setString(2, weather.getBody().getObject().getJSONObject("main").get("temp").toString());
		            preparedStatement.setString(3, weather.getBody().getObject().getJSONObject("sys").get("country").toString());
		            
		            preparedStatement.executeUpdate();
				} catch (UnirestException e) {
					// TODO Auto-generated catch block
					JFrame frame = new JFrame();
					JOptionPane.showMessageDialog(frame,
						    "Sprawdź połaczenie z internetem.",
						    "Network error",
						    JOptionPane.ERROR_MESSAGE);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		contentPane.add(btnNewButton, "cell 0 1,grow");
		
		btnReadFromDb = new JButton("Read from DB");
		btnReadFromDb.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				String str = "";
				city.clear();
				temp.clear();
				country.clear();
				try {
					preparedStatement = connect
					        .prepareStatement("select * from data");
					resultSet = preparedStatement.executeQuery();
					while (resultSet.next()) {
						city.add(resultSet.getString("city").toString());
						temp.add(resultSet.getString("temp").toString());
						country.add(resultSet.getString("country").toString());
					
					}
					for(int i=0;i<city.size();i++) {
						str+=city.get(i)+ " " +temp.get(i) + " " + country.get(i)+ "\n";
					}
					textPane.setText(str);
				} catch (SQLException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
		
		btnEmptyDb = new JButton("Empty DB");
		btnEmptyDb.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				try {
					preparedStatement = connect
					        .prepareStatement("delete from data");
					preparedStatement.executeUpdate();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
		});
		contentPane.add(btnEmptyDb, "cell 1 1");
		contentPane.add(btnReadFromDb, "cell 2 1,grow");
		
		textPane = new JTextPane();
		textPane.setEditable(false);
		contentPane.add(textPane, "cell 0 2 1 2,grow");
		textField.setEditable(false);
		contentPane.add(textField, "cell 1 2 2 1,grow");
		textField.setColumns(10);
		
		label_1 = new JLabel("");
		contentPane.add(label_1, "flowy,cell 2 3,grow");
		textPane.setAutoscrolls(true);
	}

}
