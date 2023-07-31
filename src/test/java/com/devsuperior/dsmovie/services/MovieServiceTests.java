package com.devsuperior.dsmovie.services;

import com.devsuperior.dsmovie.dto.MovieDTO;
import com.devsuperior.dsmovie.entities.MovieEntity;
import com.devsuperior.dsmovie.repositories.MovieRepository;
import com.devsuperior.dsmovie.services.exceptions.DatabaseException;
import com.devsuperior.dsmovie.services.exceptions.ResourceNotFoundException;
import com.devsuperior.dsmovie.tests.MovieFactory;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@ExtendWith(SpringExtension.class)
public class MovieServiceTests {

    @InjectMocks
    private MovieService service;

    @Mock
    private MovieRepository repository;

    private Long existingId, nonExistingId, referencedId;
    private MovieEntity movie;
    private MovieDTO dto;
    private Page<MovieEntity> page;
    private List<MovieEntity> list;
    private Pageable pageable;
    private String title;

    @BeforeEach
    void setUp() throws Exception {
        movie = MovieFactory.createMovieEntity();
        dto = MovieFactory.createMovieDTO();

        title = "Test";

        list = new ArrayList<>();
        pageable = PageRequest.of(1, 12);
        list.add(movie);
        page = new PageImpl<>(list, pageable, 1);

        existingId = 1L;
        referencedId = 2L;
        nonExistingId = 1000L;

        Mockito.when(repository.searchByTitle(ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(page);

        Mockito.when(repository.findById(existingId)).thenReturn(Optional.of(movie));
        Mockito.when(repository.findById(nonExistingId)).thenReturn(Optional.empty());

        Mockito.when(repository.save(ArgumentMatchers.any())).thenReturn(movie);

        Mockito.when(repository.getReferenceById(existingId)).thenReturn(movie);
        Mockito.when(repository.getReferenceById(nonExistingId)).thenThrow(EntityNotFoundException.class);

        Mockito.doReturn(true).when(repository).existsById(existingId);
        Mockito.doReturn(false).when(repository).existsById(nonExistingId);
        Mockito.doReturn(true).when(repository).existsById(referencedId);
        Mockito.doNothing().when(repository).deleteById(existingId);
        Mockito.doThrow(DataIntegrityViolationException.class).when(repository).deleteById(referencedId);

    }

    @Test
    public void findAllShouldReturnPagedMovieDTO() {
        Page<MovieDTO> result = service.findAll(title, pageable);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(result.get().toList().get(0).getId(), movie.getId());
    }

    @Test
    public void findByIdShouldReturnMovieDTOWhenIdExists() {
        MovieDTO result = service.findById(existingId);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(result.getId(), movie.getId());
        Assertions.assertEquals(result.getTitle(), movie.getTitle());
    }

    @Test
    public void findByIdShouldThrowResourceNotFoundExceptionWhenIdDoesNotExist() {
        Assertions.assertThrows(ResourceNotFoundException.class, () -> {
            MovieDTO result = service.findById(nonExistingId);
        });

    }

    @Test
    public void insertShouldReturnMovieDTO() {
        MovieService movieServiceSpy = Mockito.spy(service);
        MovieDTO result = movieServiceSpy.insert(dto);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(result.getId(), movie.getId());
        Assertions.assertEquals(result.getTitle(), movie.getTitle());

    }

    @Test
    public void updateShouldReturnMovieDTOWhenIdExists() {
        MovieService movieServiceSpy = Mockito.spy(service);
        MovieDTO result = movieServiceSpy.update(existingId, dto);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(result.getId(), movie.getId());
        Assertions.assertEquals(result.getTitle(), movie.getTitle());
    }

    @Test
    public void updateShouldThrowResourceNotFoundExceptionWhenIdDoesNotExist() {
        MovieService movieServiceSpy = Mockito.spy(service);
        Assertions.assertThrows(ResourceNotFoundException.class, () -> {
            MovieDTO result = movieServiceSpy.update(nonExistingId, dto);
        });
    }

    @Test
    public void deleteShouldDoNothingWhenIdExists() {
        Assertions.assertDoesNotThrow(() -> {
            service.delete(existingId);
        });
        Mockito.verify(repository, Mockito.times(1)).deleteById(existingId);
    }

    @Test
    public void deleteShouldThrowResourceNotFoundExceptionWhenIdDoesNotExist() {
        Assertions.assertThrows(ResourceNotFoundException.class, () -> {
            service.delete(nonExistingId);
        });
    }

    @Test
    public void deleteShouldThrowDatabaseExceptionWhenDependentId() {
        Assertions.assertThrows(DatabaseException.class, () -> {
            service.delete(referencedId);
        });

    }
}
