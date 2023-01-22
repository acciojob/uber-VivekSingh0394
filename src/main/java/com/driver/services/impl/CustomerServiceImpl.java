package com.driver.services.impl;

import com.driver.model.*;
import com.driver.services.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.driver.repository.CustomerRepository;
import com.driver.repository.DriverRepository;
import com.driver.repository.TripBookingRepository;

import java.util.ArrayList;
import java.util.List;

@Service
public class CustomerServiceImpl implements CustomerService {

	@Autowired
	CustomerRepository customerRepository2;

	@Autowired
	DriverRepository driverRepository2;

	@Autowired
	TripBookingRepository tripBookingRepository2;

	@Override
	public void register(Customer customer) {
		//Save the customer in database
		customerRepository2.save(customer);
	}

	@Override
	public void deleteCustomer(Integer customerId) {
		// Delete customer without using deleteById function

		Customer customer = customerRepository2.findById(customerId).get();
		customerRepository2.delete(customer);


	}

	@Override
	public TripBooking bookTrip(int customerId, String fromLocation, String toLocation, int distanceInKm) throws Exception{
		//Book the driver with lowest driverId who is free (cab available variable is Boolean.TRUE). If no driver is available, throw "No cab available!" exception
		//Avoid using SQL query
		List<Driver> driverList = driverRepository2.findAll();
		int driverId = Integer.MAX_VALUE;
		for(Driver driver : driverList)
		{
           Cab cab = driver.getCab();
		   if(cab.getAvailable()==true)
		   {
			   if(driver.getDriverId()<driverId)
			   {
				   driverId=driver.getDriverId();
			   }
		   }
		}
		if(driverId != Integer.MAX_VALUE)
		{
			// getting driver
			Driver driver = driverRepository2.findById(driverId).get();
			// getting cab
			Cab cab = driver.getCab();
			// making cab unavailable
			cab.setAvailable(false);
			// make driver unavail
        //  set cab for driver
           driver.setCab(cab);
		   // set driverid for driver
		   driver.setDriverId(driverId);


		   // set cab id for cab
		   cab .setId(cab.getCabId());

		   //cab.setDriver(driver);

			TripBooking tripBooking = new TripBooking();
			tripBooking.setCustomer(customerRepository2.findById(customerId).get());
			tripBooking.setDistanceInKm(distanceInKm);
			tripBooking.setFromLocation(fromLocation);
			tripBooking.setToLocation(toLocation);
			tripBooking.setDriver(driver);
			tripBooking.setStatus(TripStatus.CONFIRMED);




			// set triplist
		  List<TripBooking>tripBookingList= driver.getTripBookingList();
		  tripBookingList.add(tripBooking);
		  tripBookingRepository2.save(tripBooking);
		  return tripBooking;

		}
		else
			throw new Exception("No cab available!");

	}

	@Override
	public void cancelTrip(Integer tripId){
		//Cancel the trip having given trip Id and update TripBooking attributes accordingly
        TripBooking tripBooking = tripBookingRepository2.findById(tripId).get();
		// trip cancelled
		tripBooking.setStatus(TripStatus.CANCELED);
		// making cab available
		Cab cab = tripBooking.getDriver().getCab();
		cab.setAvailable(true);
       tripBookingRepository2.save(tripBooking);

	}

	@Override
	public void completeTrip(Integer tripId){
		//Complete the trip having given trip Id and update TripBooking attributes accordingly
		TripBooking tripBooking = tripBookingRepository2.findById(tripId).get();
		// trip status
		tripBooking.setStatus(TripStatus.COMPLETED);
		// per km rate
		Cab cab = tripBooking.getDriver().getCab();
		int perkmrate = cab.getPerKmRate();
		// fare
        int totalDistance = tripBooking.getDistanceInKm();
		int fare = totalDistance*perkmrate;
		// setting fare
		tripBooking.setBill(fare);


		//unavailabe
        cab.setAvailable(true);

      tripBookingRepository2.save(tripBooking);

	}
}
