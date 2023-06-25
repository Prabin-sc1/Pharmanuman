package com.pharmanuman.controller;

import java.security.Principal;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.pharmanuman.dao.MedicineRepository;
import com.pharmanuman.dao.UserRepository;
import com.pharmanuman.entities.Medicine;
import com.pharmanuman.entities.User;
import com.pharmanuman.helper.MyMessage;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/pharmacy")
public class PharmacyController {

	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private MedicineRepository medicineRepository;

	@ModelAttribute
	public void addCommonData(Model model, Principal principal) {
		String name = principal.getName();
		System.out.println("User name " + name);
		User user = this.userRepository.getUserByUserName(name);
		System.out.println("USER " + user);
		model.addAttribute("user", user);
	}

	@RequestMapping("/index")
	public String index(Model model) {
		model.addAttribute("title", "User Dashboard");
		return "pharmacy/pharmacy_dashboard";
	}

	@RequestMapping("/profile")
	public String profile(Model model) {
		model.addAttribute("title", "User Dashboard");
		return "pharmacy/profile";
	}

	@GetMapping("/order-medicine")
	public String openAddContactForm(Model model, Principal p) {
		model.addAttribute("title", "Order medicine");
		model.addAttribute("medicine", new Medicine());
		String name = p.getName();
		User tempUser = this.userRepository.getUserByUserName(name);
		List<Medicine> medicines = this.medicineRepository.findMedicinesById(tempUser.getId());
		model.addAttribute("medicines", medicines);
		return "pharmacy/add_medicine";
	}

	@RequestMapping("/process-order")
	public String processOrder(@ModelAttribute Medicine medicine, Principal p, Model m, HttpSession session) {

		try {
			String tempName = p.getName();
			User user = this.userRepository.getUserByUserName(tempName);
			medicine.setUser(user);

			user.getMedicines().add(medicine);
			this.userRepository.save(user);

			System.out.println("data: " + medicine);
			System.out.println("medicines added to database");
			String name = p.getName();
			User tempUser = this.userRepository.getUserByUserName(name);
			List<Medicine> medicines = this.medicineRepository.findMedicinesById(tempUser.getId());
			Collections.sort(medicines, Comparator.comparingInt(Medicine::getMid).reversed());
			m.addAttribute("medicines", medicines);
			session.setAttribute("msg", new MyMessage("Successfully added!! ", "alert-success"));
			return "pharmacy/add_medicine";

		} catch (Exception e) {

			m.addAttribute("medicines", medicine);
			session.setAttribute("msg", new MyMessage("Something went wrong!! " + e.getMessage(), "alert-danger"));
			return "pharmacy/add_medicine";
		}

	}

	@RequestMapping("/view-medicine")
	public String viewMedicine(Model m, Principal p) {
		m.addAttribute("title", "View Medicine");
		String name = p.getName();
		User tempUser = this.userRepository.getUserByUserName(name);
		List<Medicine> medicines = this.medicineRepository.findMedicinesById(tempUser.getId());
		m.addAttribute("medicines", medicines);
		return "pharmacy/view_medicine";
	}

	@GetMapping("/delete/{mid}")
	public String deleteMedicine(@PathVariable("mid") int mid, Model m, Principal p) {

		Medicine medicine = this.medicineRepository.findById(mid).get();
		User tempUser = this.userRepository.getUserByUserName(p.getName());
		tempUser.getMedicines().remove(medicine);
		this.userRepository.save(tempUser);
		return "redirect:/pharmacy/view-medicine";
	}

	@PostMapping("/updateMedicine/{mid}")
	public String updateMedicine(@PathVariable("mid") int id, Model m) {
		m.addAttribute("title", "Update medicine");
		Medicine tempMedicine = this.medicineRepository.findById(id).get();
		m.addAttribute("medicine", tempMedicine);
		return "pharmacy/update_medicine";
	}

	@PostMapping("/process-update")
	public String processUpdate(@ModelAttribute Medicine m, Principal p, Model model) {
//		Medicine oldMedcine = this.medicineRepository.findById(m.getMid()).get();
		String name = p.getName();
		User tempUser = this.userRepository.getUserByUserName(name);
		m.setUser(tempUser);
		this.medicineRepository.save(m);

		/*
		 * List<Medicine> medicines =
		 * this.medicineRepository.findMedicinesById(tempUser.getId());
		 * model.addAttribute("medicines", medicines);
		 */
		return "pharmacy/update_medicine";
	}

	// password change module
	@RequestMapping("/setting")
	public String setting(Model model) {
		model.addAttribute("title", "User Dashboard");
		return "pharmacy/setting";
	}

	@PostMapping("/change-password")
	public String changePassword(@RequestParam("oldPassword") String oldPassword,
			@RequestParam("newPassword") String newPassword, Principal p, HttpSession session) {
		System.out.println("Old password::" + oldPassword);
		String name = p.getName();

		User tempUser = this.userRepository.getUserByUserName(name);
		String oldPassword1 = tempUser.getPassword();

		if (this.bCryptPasswordEncoder.matches(oldPassword, oldPassword1)) {
			tempUser.setPassword(this.bCryptPasswordEncoder.encode(newPassword));
			this.userRepository.save(tempUser);

			this.userRepository.save(tempUser);
			session.setAttribute("msg", new MyMessage("Successfully Updated!! ", "alert-success"));
			return "pharmacy/setting";
		} else {
			System.out.println("password doesn't matches");
			session.setAttribute("msg", new MyMessage("Password doesn't match. ", "alert-danger"));
			return "pharmacy/setting";

		}

	}

	@RequestMapping("/medicine-details/{mid}")
	public String showMedicineDetail(@PathVariable("mid") Integer mid, Model model, Principal p) {
		System.out.println("mid " + mid);
		Optional<Medicine> medicineOptional = this.medicineRepository.findById(mid);
		Medicine medicine = medicineOptional.get();
		String name = p.getName();
		User user = this.userRepository.getUserByUserName(name);
		if (user.getId() == medicine.getUser().getId()) {
			model.addAttribute("medicine", medicine);
			model.addAttribute("title", medicine.getName());
		}

		return "pharmacy/medicine_details";
	}

}
