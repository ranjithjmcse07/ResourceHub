package com.toolshare.service;

import com.toolshare.dao.SuggestionDao;
import com.toolshare.dao.UserDao;
import com.toolshare.dto.SuggestionRequest;
import com.toolshare.dto.SuggestionStatusUpdateDto;
import com.toolshare.exception.ResourceNotFoundException;
import com.toolshare.model.Suggestion;
import com.toolshare.model.User;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SuggestionService {

    private final SuggestionDao suggestionDao;
    private final UserDao userDao;

    public SuggestionService(SuggestionDao suggestionDao, UserDao userDao) {
        this.suggestionDao = suggestionDao;
        this.userDao = userDao;
    }

    public Suggestion createSuggestion(String username, SuggestionRequest req) {
        User user = userDao.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        Suggestion s = new Suggestion();
        s.setUserId(user.getId());
        s.setSubject(req.getSubject().trim());
        s.setMessage(req.getMessage().trim());

        return suggestionDao.createSuggestion(s);
    }

    public List<Suggestion> getMySuggestions(String username) {
        User user = userDao.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
        return suggestionDao.findByUserId(user.getId());
    }

    public List<Suggestion> getAllSuggestions() {
        return suggestionDao.findAll();
    }

    public void updateSuggestionStatus(Long id, SuggestionStatusUpdateDto dto) {
        suggestionDao.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Suggestion not found with id: " + id));
        suggestionDao.updateStatusAndResponse(id, dto.getStatus(), dto.getAdminResponse());
    }
}
