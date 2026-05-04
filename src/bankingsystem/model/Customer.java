package bankingsystem.model;

public class Customer {
    private int customerId; 
    private String firstName; 
    private String lastName; 
    private String email; 
    private String phoneNumber; 

    // constructor for adding a new customer
    public Customer(String firstName, String lastName, String email, String phoneNumber) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phoneNumber = phoneNumber;
    }

    // dao logic getter and seetter
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getEmail() { return email; }
    public String getPhoneNumber() { return phoneNumber; }
}