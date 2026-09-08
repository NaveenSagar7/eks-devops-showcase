package com.myapp.dailyjournal.service;

import com.myapp.dailyjournal.model.AppUser;
import com.myapp.dailyjournal.model.Post;
import com.myapp.dailyjournal.repository.AppUserRepository;
import com.myapp.dailyjournal.repository.PostRepository;
import com.myapp.dailyjournal.util.PanUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class JournalService {

    private final AppUserRepository userRepository;
    private final PostRepository postRepository;
    private final String panSecret;

    public JournalService(AppUserRepository userRepository,
                           PostRepository postRepository,
                           @Value("${app.security.pan-secret}") String panSecret) {
        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.panSecret = panSecret;
    }

    @Transactional
    public AppUser register(String name, Integer age, LocalDate dob, String pan) {
        if (!PanUtil.isValid(pan)) {
            throw new IllegalArgumentException("PAN must look like ABCDE1234F");
        }

        String normalizedPan = PanUtil.normalize(pan);
        String panHash = PanUtil.hash(normalizedPan, panSecret);

        if (userRepository.findByPanHash(panHash).isPresent()) {
            throw new IllegalStateException("A user with this PAN is already registered");
        }

        AppUser user = new AppUser();
        user.setName(name);
        user.setAge(age);
        user.setDob(dob);
        user.setPanHash(panHash);
        user.setPanMasked(PanUtil.mask(normalizedPan));

        try {
            return userRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalStateException("A user with this PAN is already registered", e);
        }
    }

    @Transactional(readOnly = true)
    public Optional<AppUser> findByPan(String pan) {
        String panHash = PanUtil.hash(PanUtil.normalize(pan), panSecret);
        return userRepository.findByPanHash(panHash);
    }

    @Transactional(readOnly = true)
    public Optional<AppUser> findById(Long id) {
        return userRepository.findById(id);
    }

    @Transactional
    public Post addPost(AppUser user, String content) {
        Post post = new Post();
        post.setUser(user);
        post.setContent(content);
        return postRepository.save(post);
    }

    @Transactional(readOnly = true)
    public List<Post> getPosts(AppUser user) {
        return postRepository.findByUserOrderByCreatedAtDesc(user);
    }
}
