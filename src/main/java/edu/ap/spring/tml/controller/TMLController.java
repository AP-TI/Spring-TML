package edu.ap.spring.tml.controller;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import edu.ap.spring.redis.RedisService;

@Controller
public class TMLController {

    @Autowired
    private RedisService service;
    private int TOTAL_TICKETS = 5;
    private double RESERVE = 1.0;

    @GetMapping("/")
    private String getIndex() {

        return "redirect:/users";
    }

    @GetMapping("/users")
    public String getUsers() {
        return "users";
    }

    @PostMapping("/users")
    public ResponseEntity<String> addUser(@RequestParam("email") String email, @RequestParam("password") String password) {
        bytesToHex(email + password);
        service.setKey("user:" + bytesToHex(email + password)+ ":" + service.keys("user:*").size(), email);
        return ResponseEntity.ok(email + password);
    }

    @GetMapping("/login")
    public String getLogin(){
        return "login";
    }

    @PostMapping("/login")
    public String postLogin(Model model, @RequestParam("email") String email, @RequestParam("password") String password){
        boolean loggedIn = !service.keys("user:" + bytesToHex(email + password) + ":*").isEmpty();
        String page;
        if(loggedIn){
            page = "buyTicket";
            model.addAttribute("ticketLink", "/tickets/0/" + bytesToHex(email + password));
        }else{
            page = "login";
            model.addAttribute("error", "combination not found");
        }

        return page;
    }


    @GetMapping("/tickets/{event}/{user}")
    public String getTicket(@PathVariable("event") int event,
                            @PathVariable("user") String user,
                            Model model) {

        String page = "previousSale";
        String userKey = this.service.keys("user:" + user + ":*").iterator().next();
        int userId = Integer.parseInt(userKey.split(":")[2]);

        if (this.service.bitCount("event:" + event + ":users") <= TOTAL_TICKETS * RESERVE) {
            if (this.service.getBit("event:" + event + ":users", userId) != true) {
                this.service.setBit("event:" + event + ":users", userId, true);
                page = "redirect:/tickets/" + event + "/" + user + "/sale";
            }
        }

        return page;
    }

    @GetMapping("/tickets/{event}/{user}/sale")
    private String distributeTickets(@PathVariable("event") int event,
                                     @PathVariable("user") String user,
                                     Model model) {

        return "sale";
    }

    @GetMapping("/tickets/{event}")
    private String tickets(@PathVariable("event") int event, Model model) {

        model.addAttribute("ticketsCount", TOTAL_TICKETS - this.service.bitCount("event:" + event + ":users"));

        return "tickets";
    }

    private String bytesToHex(String str) {

        String retString = "";
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedhash = digest.digest((str).getBytes(StandardCharsets.UTF_8));
            StringBuffer hexString = new StringBuffer();
            for (int i = 0; i < encodedhash.length; i++) {
                String hex = Integer.toHexString(0xff & encodedhash[i]);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            retString = hexString.toString();
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }

        return retString;
    }
}
