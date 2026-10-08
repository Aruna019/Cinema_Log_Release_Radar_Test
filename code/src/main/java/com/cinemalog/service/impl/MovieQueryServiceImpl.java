
package com.cinemalog.service.impl;

import java.time.Clock;
import java.time.Instant;

import com.cinemalog.domain.entity.Movie;
import com.cinemalog.dto.request.MovieSearchCriteria;
import com.cinemalog.dto.response.MovieDetailResponse;
import com.cinemalog.dto.response.MovieSummaryResponse;
import com.cinemalog.dto.response.PageResponse;
import com.cinemalog.exception.ExternalServiceException;
import com.cinemalog.exception.ResourceNotFoundException;
import com.cinemalog.mapper.MovieMapper;
import com.cinemalog.repository.MovieRepository;
import com.cinemalog.repository.specification.MovieSpecificationBuilder;
import com.cinemalog.service.MovieQueryService;
import com.cinemalog.service.MovieSyncService;
import com.cinemalog.service.external.MovieCatalogSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MovieQueryServiceImpl implements MovieQueryService {

    private static final Logger log =
            LoggerFactory.getLogger(MovieQueryServiceImpl.class);

    private final MovieRepository movieRepository;
    private final MovieSyncService syncService;
    private final MovieCatalogSource catalogSource;
    private final MovieMapper movieMapper;
    private final Clock clock;

    public MovieQueryServiceImpl(
            MovieRepository movieRepository,
            MovieSyncService syncService,
            MovieCatalogSource catalogSource,
            MovieMapper movieMapper,
            Clock clock) {

        this.movieRepository = movieRepository;
        this.syncService = syncService;
        this.catalogSource = catalogSource;
        this.movieMapper = movieMapper;
        this.clock = clock;
    }

    @Override
    @Transactional
    public PageResponse<MovieSummaryResponse> search(MovieSearchCriteria c) {

        // เริ่มจับเวลาการค้นหาทั้งหมด
        long startTime = System.nanoTime();

        if (c.hasText() && c.page() == 0 && catalogSource.isAvailable()) {

            // จับเวลาเฉพาะการนำเข้าข้อมูลจาก TMDB
            long syncStart = System.nanoTime();

            try {
                syncService.importSearchResults(c.q());

            } catch (ExternalServiceException ex) {
                log.warn(
                        "TMDB search failed, showing local results only: {}",
                        ex.getMessage()
                );

            } finally {
                log.info(
                        "TMDB search import took {} ms",
                        elapsedMs(syncStart)
                );
            }
        }

        var spec = MovieSpecificationBuilder.create()
                .text(c.q())
                .genre(c.genreId())
                .year(c.year())
                .decade(c.decade())
                .releasedBefore(c.before())
                .minRating(c.minRating())
                .language(c.language())
                .build();

        // จับเวลาการ Query ฐานข้อมูล
        long dbStart = System.nanoTime();

        Page<Movie> page = movieRepository.findAll(
                spec,
                PageRequest.of(
                        c.page(),
                        c.size(),
                        c.sort().toSort()
                )
        );

        log.info(
                "Movie database search took {} ms",
                elapsedMs(dbStart)
        );

        // จับเวลาการแปลงข้อมูลเป็น Response
        long mappingStart = System.nanoTime();

        PageResponse<MovieSummaryResponse> response =
                PageResponse.of(
                        page,
                        movieMapper.toSummaries(page.getContent())
                );

        log.info(
                "Movie response mapping took {} ms",
                elapsedMs(mappingStart)
        );

        // เวลารวมของเมธอดค้นหา
        log.info(
                "Movie search total took {} ms",
                elapsedMs(startTime)
        );

        return response;
    }

    @Override
    @Transactional
    public MovieDetailResponse getDetail(Long movieId) {

        // เริ่มจับเวลาโหลดรายละเอียดหนัง
        long startTime = System.nanoTime();

        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Movie " + movieId + " was not found."
                        )
                );

        if (catalogSource.isAvailable()
                && movie.needsDetails(Instant.now(clock))) {

            long refreshStart = System.nanoTime();

            try {
                syncService.refreshDetails(movie);

            } catch (ExternalServiceException ex) {
                log.warn(
                        "Could not refresh details for movie {}: {}",
                        movieId,
                        ex.getMessage()
                );

            } finally {
                log.info(
                        "Movie detail TMDB refresh took {} ms",
                        elapsedMs(refreshStart)
                );
            }
        }

        MovieDetailResponse response = movieMapper.toDetail(movie);

        log.info(
                "Movie detail total took {} ms (movieId={})",
                elapsedMs(startTime),
                movieId
        );

        return response;
    }

    // แปลงระยะเวลาจาก nanoseconds เป็น milliseconds
    private long elapsedMs(long startTime) {
        return (System.nanoTime() - startTime) / 1_000_000;
    }
}
