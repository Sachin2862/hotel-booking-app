package com.hotel.controllers;

import com.braintreegateway.*;
import com.hotel.models.BookingDetails;
import com.hotel.models.HotelRooms;
import com.hotel.models.User;
import com.hotel.repository.BookingDetailsRepository;
import com.hotel.repository.HotelRoomsRepository;
import com.hotel.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.math.BigDecimal;
import java.net.URLConnection;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(value = "/hotel/")
@CrossOrigin(origins = "*", maxAge = 36000)
public class HotelController {
	
	@Autowired
	HotelRoomsRepository hotelRoomsRepository;
    @Autowired
    UserRepository userRepository;

    @Autowired
	BookingDetailsRepository bookingDetailsRepository;

	
	@RequestMapping(value = "rooms",method = RequestMethod.GET)
	List<HotelRooms> findAll() {
		return hotelRoomsRepository.findAll();
	}
	
	@RequestMapping(value = "add", method = RequestMethod.POST)
	public HotelRooms addHotel(@RequestBody HotelRooms hotelRooms) {
		hotelRoomsRepository.save(hotelRooms);
		return hotelRoomsRepository.save(hotelRooms);
	}

	@RequestMapping(value = "getById/{id}", method = RequestMethod.GET)
	public HotelRooms addHotel(@PathVariable("id") Long id ) {
		Optional<HotelRooms> hotelRoom = hotelRoomsRepository.findById(id);
		if(hotelRoom.isPresent())
		return hotelRoom.get();
		else
			return null;
	}

	@RequestMapping(value = "getByUserId/{id}", method = RequestMethod.GET)
	public List<BookingDetails> getByUserId(@PathVariable("id") Long id ) {
		List<BookingDetails> bookingDetails = bookingDetailsRepository.findByUserId(id);
		return bookingDetails;
	}

	@RequestMapping(value = "bookingDetails", method = RequestMethod.GET)
	public List<BookingDetails> getAll() {
		List<BookingDetails> bookingDetails = bookingDetailsRepository.findAll();
		return bookingDetails;

	}

	@RequestMapping(value = "display/{fileName:.+}", method = RequestMethod.GET)
	public void download(HttpServletRequest request, HttpServletResponse response,
			@PathVariable("fileName") String fileName) throws IOException {
		//File file = new File("/home/ec2-user/images/" + fileName);
		File file = new File("/home/site/wwwroot/images/" + fileName);
		if (file.exists()) {
			String mimeType = URLConnection.guessContentTypeFromName(file.getName());
			if (mimeType == null) {
				mimeType = "application/octet-stream";
			}
			response.setContentType(mimeType);
			response.setContentLength((int) file.length());
			response.setHeader("Content-Disposition", String.format("inline; filename=\"" + file.getName() + "\""));
			InputStream inputStream = new BufferedInputStream(new FileInputStream(file));
			FileCopyUtils.copy(inputStream, response.getOutputStream());

		}
	}


	@PostMapping("create/clientToken")
	public Customer braintree(@RequestBody Customer customer) {
		BraintreeGateway gateway = new BraintreeGateway(
				Environment.SANDBOX,
				"rsvzqb3hvcmbp7dw",
				"pdv56nqmzx838w3z",
				"a5c3f637565803c644e069cfc7a86936"
		);

		ClientTokenRequest clientTokenRequest = new ClientTokenRequest();
				//.customerId(customer.getId().toString());
// pass clientToken to your front-end
		String clientToken = gateway.clientToken().generate(clientTokenRequest);
		customer.setClientToken(clientToken);
		return customer;

	}


	@PostMapping("transactionRequest")
	public Result transactionRequest(@RequestBody Customer customer) {
		BraintreeGateway gateway = new BraintreeGateway(
				Environment.SANDBOX,
				"rsvzqb3hvcmbp7dw",
				"pdv56nqmzx838w3z",
				"a5c3f637565803c644e069cfc7a86936"
		);

		Optional<User> user  = userRepository.findById(customer.getId());
		 if(!user.isPresent()){
             // NOt Exist
         }
        Optional<HotelRooms> hotelRooms
                =   hotelRoomsRepository.findById(customer.getBookingInfo().getHotelId());
        if(!hotelRooms.isPresent()){
			// NOt Exist
        }
		Instant startDate = Instant.parse ( customer.getBookingInfo().getStartDate());
		Instant endDate = Instant.parse ( customer.getBookingInfo().getEndDate());
		long days = ChronoUnit.DAYS.between(startDate,endDate);
		Double totalAmount = (hotelRooms.get().getPrice() * Double.valueOf(days));
		TransactionRequest request = new TransactionRequest()
				.amount(new BigDecimal(totalAmount))
				.paymentMethodNonce(customer.getNonceFromTheClient())
				.deviceData("Test Data")
				.options()
				.submitForSettlement(true)
				.done();

		Result<Transaction> result = gateway.transaction().sale(request);
		if(result.isSuccess()){
            BookingDetails bookingDetails = new BookingDetails();
             bookingDetails.setHotelRooms(hotelRooms.get());
            bookingDetails.setAmount(totalAmount);
            bookingDetails.setHotelId(hotelRooms.get().getId());
            bookingDetails.setUserId(user.get().getId());
            bookingDetails.setCheckIn(startDate.toString());
            bookingDetails.setCheckOut(endDate.toString());
            bookingDetails.setCreated(System.currentTimeMillis());
            bookingDetails.setNumberOfDays(Integer.valueOf(String.valueOf(days)));
            bookingDetails.setBraintreePaymentDetail(result);

            bookingDetailsRepository.save(bookingDetails);
        }
		return result;

	}
	static  class BookingInfo{
		Long hotelId;
		String startDate;
		String endDate;
		Double amount;
		public Double getAmount() {
			return amount;
		}

		public void setAmount(Double amount) {
			this.amount = amount;
		}

		public Long getHotelId() {
			return hotelId;
		}

		public void setHotelId(Long hotelId) {
			this.hotelId = hotelId;
		}


		public String getStartDate() {
			return startDate;
		}

		public void setStartDate(String startDate) {
			this.startDate = startDate;
		}

		public String getEndDate() {
			return endDate;
		}

		public void setEndDate(String endDate) {
			this.endDate = endDate;
		}
	}


	static class Customer {
		Long id;
		String clientToken;
		String nonceFromTheClient;
		BookingInfo bookingInfo;

		public BookingInfo getBookingInfo() {
			return bookingInfo;
		}

		public void setBookingInfo(BookingInfo bookingInfo) {
			this.bookingInfo = bookingInfo;
		}

		public String getNonceFromTheClient() {
			return nonceFromTheClient;
		}

		public void setNonceFromTheClient(String nonceFromTheClient) {
			this.nonceFromTheClient = nonceFromTheClient;
		}

		public String getClientToken() {
			return clientToken;
		}

		public void setClientToken(String clientToken) {
			this.clientToken = clientToken;
		}

		public Long getId() {
			return id;
		}

		public void setId(Long id) {
			this.id = id;
		}
	}
}
