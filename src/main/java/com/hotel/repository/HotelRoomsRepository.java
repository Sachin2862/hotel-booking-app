package com.hotel.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hotel.models.HotelRooms;
import com.hotel.models.User;

@Repository
public interface HotelRoomsRepository extends JpaRepository<HotelRooms, Long> {
	
}
