package com.myapp.dailyjournal.controller;

import com.myapp.dailyjournal.model.AppUser;
import com.myapp.dailyjournal.service.JournalService;
import com.myapp.dailyjournal.web.PostForm;
import com.myapp.dailyjournal.web.RegisterForm;
import com.myapp.dailyjournal.web.ReturningForm;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Controller
public class HomeController {

    private static final String SESSION_USER_ID = "userId";

    private final JournalService journalService;

    public HomeController(JournalService journalService) {
        this.journalService = journalService;
    }

    @GetMapping("/")
    public String welcome() {
        return "welcome";
    }

    @GetMapping("/register")
    public String registerForm(Model model) {
        if (!model.containsAttribute("registerForm")) {
            model.addAttribute("registerForm", new RegisterForm());
        }
        return "register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("registerForm") RegisterForm form,
                            BindingResult bindingResult,
                            HttpSession session,
                            Model model) {
        if (bindingResult.hasErrors()) {
            return "register";
        }
        try {
            AppUser user = journalService.register(form.getName(), form.getAge(), form.getDob(), form.getPan());
            session.setAttribute(SESSION_USER_ID, user.getId());
            return "redirect:/post/new";
        } catch (IllegalStateException | IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "register";
        }
    }

    @GetMapping("/returning")
    public String returningForm(Model model) {
        if (!model.containsAttribute("returningForm")) {
            model.addAttribute("returningForm", new ReturningForm());
        }
        return "returning";
    }

    @PostMapping("/returning")
    public String returning(@Valid @ModelAttribute("returningForm") ReturningForm form,
                             BindingResult bindingResult,
                             HttpSession session,
                             Model model) {
        if (bindingResult.hasErrors()) {
            return "returning";
        }
        Optional<AppUser> user = journalService.findByPan(form.getPan());
        if (user.isEmpty()) {
            model.addAttribute("errorMessage", "We couldn't find that PAN. New here? Register instead.");
            return "returning";
        }
        session.setAttribute(SESSION_USER_ID, user.get().getId());
        return "redirect:/dashboard";
    }

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        AppUser user = requireUser(session);
        if (user == null) {
            return "redirect:/";
        }
        model.addAttribute("user", user);
        return "dashboard";
    }

    @GetMapping("/post/new")
    public String newPostForm(HttpSession session, Model model) {
        AppUser user = requireUser(session);
        if (user == null) {
            return "redirect:/";
        }
        model.addAttribute("user", user);
        if (!model.containsAttribute("postForm")) {
            model.addAttribute("postForm", new PostForm());
        }
        return "write-post";
    }

    @PostMapping("/post/new")
    public String createPost(@Valid @ModelAttribute("postForm") PostForm form,
                              BindingResult bindingResult,
                              HttpSession session,
                              Model model) {
        AppUser user = requireUser(session);
        if (user == null) {
            return "redirect:/";
        }
        if (bindingResult.hasErrors()) {
            model.addAttribute("user", user);
            return "write-post";
        }
        journalService.addPost(user, form.getContent());
        return "redirect:/dashboard";
    }

    @GetMapping("/posts")
    public String posts(HttpSession session, Model model) {
        AppUser user = requireUser(session);
        if (user == null) {
            return "redirect:/";
        }
        model.addAttribute("user", user);
        model.addAttribute("posts", journalService.getPosts(user));
        return "posts";
    }

    @PostMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }

    private AppUser requireUser(HttpSession session) {
        Object id = session.getAttribute(SESSION_USER_ID);
        if (id == null) {
            return null;
        }
        return journalService.findById((Long) id).orElse(null);
    }
}
