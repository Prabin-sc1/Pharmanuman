package com.pharmanuman.controller;

import java.security.Principal;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.pharmanuman.dao.MedicineRepository;
import com.pharmanuman.dao.UserRepository;
import com.pharmanuman.entities.Medicine;
import com.pharmanuman.entities.User;

@Controller
@RequestMapping("/pharmacy")
public class PharmacyController {

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

	@GetMapping("/order-medicine")
	public String openAddContactForm(Model model, Principal p) {
		model.addAttribute("title", "Order medicine");
		model.addAttribute("medicine", new Medicine());
		String name = p.getName();
		User tempUser = this.userRepository.getUserByUserName(name);
		List<Medicine> medicines = this.medicineRepository.findMedicinesById(tempUser.getId());
		model.addAttribute("medicines", medicines);
		return "pharmacy/order_medicine";
	}

	@RequestMapping("/process-order")
	public String processOrder(@ModelAttribute Medicine medicine, Principal p, Model m) {

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
		return "pharmacy/order_medicine";
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

	@GetMapping("/deleteProcessOrder/{mid}")
	public String deleteMedicineFromProcessOrder(@PathVariable("mid") int mid, Model m, Principal p) {

		Medicine medicine = this.medicineRepository.findById(mid).get();
		User tempUser = this.userRepository.getUserByUserName(p.getName());
		tempUser.getMedicines().remove(medicine);
		this.userRepository.save(tempUser);
		
		String name = p.getName();
		User tempUser1 = this.userRepository.getUserByUserName(name);
		List<Medicine> medicines = this.medicineRepository.findMedicinesById(tempUser1.getId());
		m.addAttribute("medicines", medicines);
		return "pharmacy/order_medicine";
	}

}
