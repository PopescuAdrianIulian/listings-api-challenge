package com.challenge.listings_api.repository;

import com.challenge.listings_api.entity.Listing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;


@Repository
public interface ListingRepository extends JpaRepository<Listing, String>, JpaSpecificationExecutor<Listing> {

    @Query(value = """
        SELECT 
            AVG(lat) as lat, 
            AVG(lon) as lon, 
            COUNT(*) as count
        FROM listings
        WHERE (lat BETWEEN :minLat AND :maxLat) 
          AND (lon BETWEEN :minLon AND :maxLon)
          AND (:type IS NULL OR listing_type = :type)
          AND (:minRooms IS NULL OR rooms >= :minRooms)
          AND (:maxRooms IS NULL OR rooms <= :maxRooms)
          AND (:minPrice IS NULL OR price >= :minPrice)
          AND (:maxPrice IS NULL OR price <= :maxPrice)
        GROUP BY 
            FLOOR((lat - :minLat) / ((:maxLat - :minLat) / :gridSize)),
            FLOOR((lon - :minLon) / ((:maxLon - :minLon) / :gridSize))
        LIMIT :maxClusters
        """, nativeQuery = true)
    List<ClusterProjection> getClustersNative(
            @Param("minLat") Double minLat, @Param("maxLat") Double maxLat,
            @Param("minLon") Double minLon, @Param("maxLon") Double maxLon,
            @Param("type") String type,
            @Param("minRooms") Integer minRooms, @Param("maxRooms") Integer maxRooms,
            @Param("minPrice") Double minPrice, @Param("maxPrice") Double maxPrice,
            @Param("gridSize") int gridSize,
            @Param("maxClusters") int maxClusters
    );
}