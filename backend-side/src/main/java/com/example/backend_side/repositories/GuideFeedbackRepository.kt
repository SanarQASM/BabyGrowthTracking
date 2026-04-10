package com.example.backend_side.repositories

import com.example.backend_side.entity.GuideFeedback
import com.example.backend_side.entity.GuideType
import com.example.backend_side.entity.VoteType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface GuideFeedbackRepository : JpaRepository<GuideFeedback, String> {

    /**
     * Returns the single feedback row a user has cast for a specific content
     * item within a specific guide, or empty if they have not voted yet.
     */
    fun findByUser_UserIdAndContentIdAndGuideType(
        userId    : String,
        contentId : String,
        guideType : GuideType
    ): Optional<GuideFeedback>

    /**
     * Counts votes of a specific type for a content item.
     * Used after insert/update/delete to return the real post-mutation count.
     */
    @Query("""
        SELECT COUNT(gf)
        FROM   GuideFeedback gf
        WHERE  gf.contentId = :contentId
        AND    gf.guideType = :guideType
        AND    gf.vote      = :vote
    """)
    fun countByContentIdAndGuideTypeAndVote(
        @Param("contentId") contentId : String,
        @Param("guideType") guideType : GuideType,
        @Param("vote")      vote      : VoteType
    ): Long

    /**
     * Bulk-fetch counts for a list of content IDs — avoids N+1 queries when
     * rendering a list of guide cards.
     * Returns rows of (contentId, voteType, count).
     */
    @Query("""
        SELECT gf.contentId, gf.vote, COUNT(gf)
        FROM   GuideFeedback gf
        WHERE  gf.contentId IN :contentIds
        AND    gf.guideType  = :guideType
        GROUP BY gf.contentId, gf.vote
    """)
    fun countsByContentIdsAndGuideType(
        @Param("contentIds") contentIds : List<String>,
        @Param("guideType")  guideType  : GuideType
    ): List<Array<Any>>

    /**
     * Returns the vote the given user cast for each of the supplied content IDs.
     * Useful for restoring the voted state when the user reopens the guide.
     */
    @Query("""
        SELECT gf
        FROM   GuideFeedback gf
        WHERE  gf.user.userId = :userId
        AND    gf.contentId  IN :contentIds
        AND    gf.guideType   = :guideType
    """)
    fun findUserVotesForContents(
        @Param("userId")     userId     : String,
        @Param("contentIds") contentIds : List<String>,
        @Param("guideType")  guideType  : GuideType
    ): List<GuideFeedback>

    /**
     * FIX: Direct delete by composite key — used when a user toggles off
     * their vote (taps the same button again). JpaRepository.delete(entity)
     * also works (used in the service layer) since the entity is already
     * loaded. This method is provided as an efficient alternative when
     * you don't want to load the entity first.
     */
    @Modifying
    @Query("""
        DELETE FROM GuideFeedback gf
        WHERE  gf.user.userId = :userId
        AND    gf.contentId   = :contentId
        AND    gf.guideType   = :guideType
    """)
    fun deleteByUserAndContentAndGuideType(
        @Param("userId")    userId    : String,
        @Param("contentId") contentId : String,
        @Param("guideType") guideType : GuideType
    )
}