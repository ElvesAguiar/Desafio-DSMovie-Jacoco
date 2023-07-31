package com.devsuperior.dsmovie.services;

import com.devsuperior.dsmovie.dto.MovieDTO;
import com.devsuperior.dsmovie.dto.ScoreDTO;
import com.devsuperior.dsmovie.entities.MovieEntity;
import com.devsuperior.dsmovie.entities.ScoreEntity;
import com.devsuperior.dsmovie.entities.UserEntity;
import com.devsuperior.dsmovie.repositories.MovieRepository;
import com.devsuperior.dsmovie.repositories.ScoreRepository;
import com.devsuperior.dsmovie.services.exceptions.ResourceNotFoundException;
import com.devsuperior.dsmovie.tests.MovieFactory;
import com.devsuperior.dsmovie.tests.ScoreFactory;
import com.devsuperior.dsmovie.tests.UserFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

@ExtendWith(SpringExtension.class)
public class ScoreServiceTests {

    @InjectMocks
    private ScoreService service;

    @Mock
    private UserService userService;

    @Mock
    private MovieRepository movieRepository;

    @Mock
    private ScoreRepository scoreRepository;

    private MovieEntity movie;
    private MovieDTO movieDTO;
    private ScoreEntity score;
    private ScoreDTO scoreDTO;
    private UserEntity user;

    private Long existingId, nonExistingId;

    @BeforeEach
    void setUp() {

        existingId = 1L;
        nonExistingId = 1000L;
        movie = MovieFactory.createMovieEntity();
        movieDTO = MovieFactory.createMovieDTO();
        score = ScoreFactory.createScoreEntity();
        movie.getScores().add(score);
        scoreDTO = ScoreFactory.createScoreDTO();
        user = UserFactory.createUserEntity();

        Mockito.when(userService.authenticated()).thenReturn(user);


    }

    @Test
    public void saveScoreShouldReturnMovieDTO() {

        Mockito.when(movieRepository.findById(existingId)).thenReturn(Optional.of(movie));
        Mockito.when(scoreRepository.saveAndFlush(ArgumentMatchers.any())).thenReturn(score);
        Mockito.when(movieRepository.save(movie)).thenReturn(movie);

        MovieDTO result = service.saveScore(scoreDTO);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(result.getId(), movie.getId());

    }

    @Test
    public void saveScoreShouldThrowResourceNotFoundExceptionWhenNonExistingMovieId() {


        Mockito.when(movieRepository.findById(nonExistingId)).thenReturn(Optional.empty());

        Assertions.assertThrows(ResourceNotFoundException.class, () -> {
            MovieDTO result = service.saveScore(scoreDTO);
        });
    }
}
