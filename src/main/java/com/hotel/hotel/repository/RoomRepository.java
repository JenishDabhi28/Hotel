package com.hotel.hotel.repository;

import com.hotel.hotel.model.Room;
import jakarta.persistence.criteria.From;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface RoomRepository extends JpaRepository<Room, Long> {
    @Query("SELECT distinct r.roomType From Room r")
    List<String> findDistinctRoomTypes();

    List<Room> findByRoomType(String roomType);
}
