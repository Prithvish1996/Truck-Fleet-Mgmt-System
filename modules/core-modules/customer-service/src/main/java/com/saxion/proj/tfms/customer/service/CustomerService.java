package com.saxion.proj.tfms.customer.service;

import com.saxion.proj.tfms.customer.model.Customer;
import com.saxion.proj.tfms.customer.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CustomerService {

    @Autowired
    private CustomerRepository customerRepository;

    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

    public Optional<Customer> getCustomerById(Long id) {
        return customerRepository.findById(id);
    }

    public Optional<Customer> getCustomerByEmail(String email) {
        return customerRepository.findByEmail(email);
    }

    public List<Customer> searchCustomersByName(String name) {
        return customerRepository.findByNameContainingIgnoreCase(name);
    }

    public Customer createCustomer(Customer customer) {
        if (customerRepository.existsByEmail(customer.getEmail())) {
            throw new RuntimeException("Customer with email " + customer.getEmail() + " already exists");
        }
        return customerRepository.save(customer);
    }

    public Customer updateCustomer(Long id, Customer customerDetails) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Customer not found with id " + id));

        customer.setName(customerDetails.getName());
        customer.setPhone(customerDetails.getPhone());
        customer.setAddress(customerDetails.getAddress());
        
        // Only update email if it's different and not already taken by another customer
        if (!customer.getEmail().equals(customerDetails.getEmail())) {
            if (customerRepository.existsByEmail(customerDetails.getEmail())) {
                throw new RuntimeException("Email " + customerDetails.getEmail() + " is already taken");
            }
            customer.setEmail(customerDetails.getEmail());
        }

        return customerRepository.save(customer);
    }

    public void deleteCustomer(Long id) {
        if (!customerRepository.existsById(id)) {
            throw new RuntimeException("Customer not found with id " + id);
        }
        customerRepository.deleteById(id);
    }
}
