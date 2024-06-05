package com.hotel.hotel.controller;
import com.hotel.hotel.exception.ResourceNotFoundException;
import com.hotel.hotel.model.BookedRoom;
import com.hotel.hotel.model.Room;
import com.hotel.hotel.exception.PhotoRetrievalException;
import com.hotel.hotel.response.RoomResponse;
import com.hotel.hotel.service.BookingService;
import com.hotel.hotel.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import javax.sql.rowset.serial.SerialBlob;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@CrossOrigin(origins = "http://localhost:5174")
@RequiredArgsConstructor
@RestController
@RequestMapping("/rooms")
public class RoomController {
    private final RoomService roomService;
    private final BookingService bookedRoomService;
    @PostMapping("/add/new")
    public ResponseEntity<?> addNewRoom( @RequestParam("photo") MultipartFile photo,
                                                    @RequestParam("roomType") String roomType,
                                                    @RequestParam("roomPrice")BigDecimal roomPrice ) throws SQLException, IOException {
        Room savedRoom = roomService.addNewRoom(photo,roomType,roomPrice);
        RoomResponse response = new RoomResponse(savedRoom.getId(), savedRoom.getRoomType(),
                savedRoom.getRoomPrice());
        return ResponseEntity.ok(response);

    }
    @GetMapping("/room/types")
    public List<String> getRoomTypes(){
        return roomService.getAllRoomTypes();

    }


    @GetMapping("/all-room")
    public ResponseEntity<List<RoomResponse>> getAllRooms() throws SQLException {
        List<Room> rooms = roomService.getAllRooms();
        List<RoomResponse> roomResponses = new ArrayList<>();
        for(Room room : rooms){
            byte[] photo = roomService.getRoomPhotoByRoomId(room.getId());
            if(photo != null && photo.length>0){
                String base64Photo = Base64.encodeBase64String(photo);
                RoomResponse roomResponse = getRoomResponse(room);
                roomResponse.setPhoto(base64Photo);
                roomResponses.add(roomResponse);
            }
        }
        return ResponseEntity.ok(roomResponses);
    }

    @DeleteMapping("/delete/room/{roomId}")
    public ResponseEntity<Void> deleteRoom(@PathVariable Long roomId){
    roomService.deleteRoom(roomId);
    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PutMapping("/update/{roomId}")
    public ResponseEntity<RoomResponse> updateRoom(@PathVariable Long roomId, @RequestParam(required = false) MultipartFile photo,
                                         @RequestParam(required = false) String roomType,
                                         @RequestParam(required = false)BigDecimal roomPrice ) throws IOException, SQLException {
        byte[] photoByte = photo != null && !photo.isEmpty()?
                photo.getBytes(): roomService.getRoomPhotoByRoomId(roomId);
    Blob photoBlob = photoByte != null && photoByte.length>0 ? new SerialBlob(photoByte): null;
    Room theRoom = roomService.updateRoom(roomId, roomType, roomPrice, photoByte);
    theRoom.setPhoto(photoBlob);
    RoomResponse roomResponse = getRoomResponse(theRoom);
    return ResponseEntity.ok(roomResponse);
    }

    @GetMapping("/room/{roomId}")
    public  ResponseEntity<Optional<RoomResponse>> getRoomById(@PathVariable Long roomId){
        Optional<Room> theRoom = roomService.getRoomById(roomId);
        return theRoom.map(room -> {
            RoomResponse roomResponse= getRoomResponse(room);
            return ResponseEntity.ok(Optional.of(roomResponse));
        }).orElseThrow(()->new ResourceNotFoundException("Room not found"));


    }


    @GetMapping("/available-rooms")
    public ResponseEntity<List<RoomResponse>> getAvailableRooms(@RequestParam String checkInDate,
                                                                @RequestParam String checkOutDate,
                                                                @RequestParam String roomType) throws SQLException {
        List<Room> rooms = roomService.findAvailableRooms(checkInDate, checkOutDate, roomType);
        List<RoomResponse> roomResponses = new ArrayList<>();
        for (Room room : rooms) {
            byte[] photo = roomService.getRoomPhotoByRoomId(room.getId());
            if (photo != null && photo.length > 0) {
                String base64Photo = Base64.encodeBase64String(photo);
                RoomResponse roomResponse = getRoomResponse(room);
                roomResponse.setPhoto(base64Photo);
                roomResponses.add(roomResponse);
            }
        }
        return ResponseEntity.ok(roomResponses);
    }
    private RoomResponse getRoomResponse(Room room) {
        List<BookedRoom> bookings = getAllBookingByRoomId(room.getId());
  /*      List<BookedRoomResponse> bookingInfo= bookings
                .stream()
                .map(booking -> new BookedRoomResponse(booking.getBookingId(),
                        booking.getCheckInDate(),
                        booking.getCheckOutDate(),
                        booking.getBookingConfirmatiomCode())).toList();*/
        byte[] photo = null;
        Blob photoBlob =room.getPhoto();
        if(photoBlob !=null){
            try{
                photo=photoBlob.getBytes(1,(int) photoBlob.length());
            }
            catch(SQLException e){
                throw new PhotoRetrievalException("Error Retrieving photo");
            }
        }
        return new RoomResponse(room.getId(), room.getRoomType(), room.getRoomPrice(), room.isBooked(),photo);
    }

    private List<BookedRoom> getAllBookingByRoomId(Long roomId) {
        return bookedRoomService.getAllBookingByRoomId(roomId);
    }
}

















