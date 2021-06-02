package com.hotel.repository;

import com.hotel.models.BookingDetails;
import com.hotel.models.HotelRooms;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingDetailsRepository extends JpaRepository<BookingDetails, Long> {

    List<BookingDetails> findByUserId(Long id);
	
}
