package com.hotel.hotel.service;

import com.hotel.hotel.model.BookedRoom;
import com.hotel.hotel.model.Room;

import java.util.List;

public interface BookedRoomService {
    public List<BookedRoom> getAllBookingByRoomId(Long roomId);

}
