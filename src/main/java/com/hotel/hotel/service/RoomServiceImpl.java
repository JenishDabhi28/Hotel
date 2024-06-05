package com.hotel.hotel.service;

import com.hotel.hotel.exception.InternalServerException;
import com.hotel.hotel.exception.ResourceNotFoundException;
import com.hotel.hotel.model.Room;
import com.hotel.hotel.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.config.ConfigDataResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.sql.rowset.serial.SerialBlob;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RoomServiceImpl implements RoomService{
    private final RoomRepository roomRepository;
    @Override
    public Room addNewRoom(MultipartFile file, String roomType, BigDecimal roomPrice) throws SQLException, IOException {
        Room room = new Room();
        room.setRoomType(roomType);
        room.setRoomPrice(roomPrice);
        if(!file.isEmpty()){
            byte[] photo = file.getBytes();
            Blob photoBlob = new SerialBlob(photo);
            room.setPhoto(photoBlob);
        }
        return roomRepository.save(room);
    }

    @Override
    public List<String> getAllRoomTypes() {

        return roomRepository.findDistinctRoomTypes();
    }

    @Override
    public List<Room> getAllRooms() {
        return roomRepository.findAll();
    }

    @Override
    public byte[] getRoomPhotoByRoomId(Long roomId) throws SQLException {
        Optional<Room> theRoom = roomRepository.findById(roomId);
        if(theRoom.isEmpty()){
            throw new ResourceNotFoundException("Sorry, Room Not Found");
        }
        Blob photo = theRoom.get().getPhoto();
        if(photo != null){
            return photo.getBytes(1,(int) photo.length());
        }
        return new byte[0];
    }

    @Override
    public void deleteRoom(Long roomId) {
        Optional<Room> theRoom = roomRepository.findById(roomId);
        if(theRoom.isPresent()){
            roomRepository.deleteById(roomId);
        }
    }

    @Override
    public Room updateRoom(Long roomId, String roomType, BigDecimal roomPrice, byte[] photoByte) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room Not Found"));
        if (roomType!= null) room.setRoomType(roomType);
        if(roomPrice!= null) room.setRoomPrice(roomPrice);
        if(photoByte!= null && photoByte.length>0) {

            try{
                room.setPhoto(new SerialBlob(photoByte));

            }
            catch (SQLException ex){
                throw new InternalServerException("Error Updating room");

            }
        }
        return roomRepository.save(room);
    }

    @Override
    public Optional<Room> getRoomById(Long roomId) {
        return Optional.of(roomRepository.findById(roomId).get());
    }

    @Override
    public List<Room> findAvailableRooms(String checkInDate, String checkOutDate, String roomType) {
        LocalDate checkIn = LocalDate.parse(checkInDate);
        LocalDate checkOut = LocalDate.parse(checkOutDate);
        // Fetch rooms by room type
        List<Room> rooms = roomRepository.findByRoomType(roomType);
        // Filter out rooms that are booked within the date range
        return rooms.stream()
                .filter(room -> room.getBookings().stream()
                        .noneMatch(booking -> !(booking.getCheckOutDate().isBefore(checkIn) || booking.getCheckInDate().isAfter(checkOut))))
                .toList();
    }
}
