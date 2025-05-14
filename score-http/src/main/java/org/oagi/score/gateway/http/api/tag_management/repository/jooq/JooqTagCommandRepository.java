package org.oagi.score.gateway.http.api.tag_management.repository.jooq;

import jakarta.annotation.Nullable;
import org.jooq.DSLContext;
import org.jooq.UpdateSetMoreStep;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.bccp.BccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtManifestId;
import org.oagi.score.gateway.http.api.tag_management.model.TagId;
import org.oagi.score.gateway.http.api.tag_management.repository.TagCommandRepository;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.JooqBaseRepository;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.oagi.score.gateway.http.common.repository.jooq.entity.tables.records.*;

import java.time.LocalDateTime;
import java.util.regex.Pattern;

import static org.jooq.impl.DSL.and;
import static org.oagi.score.gateway.http.common.repository.jooq.entity.Tables.*;
import static org.springframework.util.StringUtils.hasLength;

/**
 * Jooq-based implementation of the {@link TagCommandRepository} for managing tag-related data.
 */
public class JooqTagCommandRepository extends JooqBaseRepository implements TagCommandRepository {

    public JooqTagCommandRepository(DSLContext dslContext, ScoreUser requester,
                                    RepositoryFactory repositoryFactory) {
        super(dslContext, requester, repositoryFactory);
    }

    @Override
    public TagId create(String name,
                        String textColor,
                        String backgroundColor,
                        @Nullable String description) {

        if (!hasLength(name)) {
            throw new IllegalArgumentException("`tagId` must not be null.");
        }
        if (!hasLength(textColor)) {
            throw new IllegalArgumentException("`textColor` must not be null.");
        }
        if (!isValidColor(textColor)) {
            throw new IllegalArgumentException("`textColor` must be in valid RGB hex format (e.g., #FFFFFF).");
        }
        if (!hasLength(backgroundColor)) {
            throw new IllegalArgumentException("`backgroundColor` must not be null.");
        }
        if (!isValidColor(backgroundColor)) {
            throw new IllegalArgumentException("`backgroundColor` must be in valid RGB hex format (e.g., #FFFFFF).");
        }

        LocalDateTime timestamp = LocalDateTime.now();

        TagRecord labelRecord = new TagRecord();
        labelRecord.setName(name);
        labelRecord.setDescription(description);
        labelRecord.setTextColor(textColor);
        labelRecord.setBackgroundColor(backgroundColor);
        labelRecord.setCreatedBy(valueOf(requester().userId()));
        labelRecord.setLastUpdatedBy(valueOf(requester().userId()));
        labelRecord.setCreationTimestamp(timestamp);
        labelRecord.setLastUpdateTimestamp(timestamp);

        return new TagId(
                dslContext().insertInto(TAG)
                        .set(labelRecord)
                        .returning(TAG.TAG_ID)
                        .fetchOne().getTagId().toBigInteger()
        );
    }

    /**
     * Validates if the given color is in valid RGB hex format (e.g., '#FFFFFF').
     *
     * @param color The color string to validate.
     * @return {@code true} if the color is in a valid RGB hex format, {@code false} otherwise.
     */
    private boolean isValidColor(String color) {
        if (color == null) {
            return false;
        }
        String regex = "^#[0-9A-Fa-f]{1,6}$"; // Regex for validating RGB hex color format
        Pattern pattern = Pattern.compile(regex);
        return pattern.matcher(color).matches();
    }

    @Override
    public boolean update(TagId tagId,
                          String name,
                          @Nullable String textColor,
                          @Nullable String backgroundColor,
                          @Nullable String description) {

        if (tagId == null) {
            throw new IllegalArgumentException("`tagId` must not be null.");
        }
        if (!hasLength(name)) {
            throw new IllegalArgumentException("`name` must not be null.");
        }
        if (hasLength(textColor) && !isValidColor(textColor)) {
            throw new IllegalArgumentException("`textColor` must be in valid RGB hex format (e.g., #FFFFFF).");
        }
        if (hasLength(backgroundColor) && !isValidColor(backgroundColor)) {
            throw new IllegalArgumentException("`backgroundColor` must be in valid RGB hex format (e.g., #FFFFFF).");
        }

        UpdateSetMoreStep moreStep = dslContext().update(TAG)
                .set(TAG.NAME, name);
        if (hasLength(textColor)) {
            moreStep = moreStep.set(TAG.TEXT_COLOR, textColor);
        }
        if (hasLength(backgroundColor)) {
            moreStep = moreStep.set(TAG.BACKGROUND_COLOR, backgroundColor);
        }
        if (hasLength(description)) {
            moreStep = moreStep.set(TAG.DESCRIPTION, description);
        } else {
            moreStep = moreStep.setNull(TAG.DESCRIPTION);
        }

        int numOfUpdatedRecords = moreStep
                .set(TAG.LAST_UPDATED_BY, valueOf(requester().userId()))
                .set(TAG.LAST_UPDATE_TIMESTAMP, LocalDateTime.now())
                .where(TAG.TAG_ID.eq(valueOf(tagId)))
                .execute();
        return numOfUpdatedRecords == 1;
    }

    @Override
    public boolean delete(TagId tagId) {

        if (tagId == null) {
            throw new IllegalArgumentException("`tagId` must not be null.");
        }

        dslContext().deleteFrom(ACC_MANIFEST_TAG)
                .where(ACC_MANIFEST_TAG.TAG_ID.eq(valueOf(tagId)))
                .execute();
        dslContext().deleteFrom(ASCCP_MANIFEST_TAG)
                .where(ASCCP_MANIFEST_TAG.TAG_ID.eq(valueOf(tagId)))
                .execute();
        dslContext().deleteFrom(BCCP_MANIFEST_TAG)
                .where(BCCP_MANIFEST_TAG.TAG_ID.eq(valueOf(tagId)))
                .execute();
        dslContext().deleteFrom(DT_MANIFEST_TAG)
                .where(DT_MANIFEST_TAG.TAG_ID.eq(valueOf(tagId)))
                .execute();

        int numOfUpdatedRecords = dslContext().deleteFrom(TAG)
                .where(TAG.TAG_ID.eq(valueOf(tagId)))
                .execute();
        return numOfUpdatedRecords == 1;
    }

    @Override
    public boolean addTag(TagId tagId, AccManifestId accManifestId) {

        if (tagId == null) {
            throw new IllegalArgumentException("`tagId` must not be null.");
        }
        if (accManifestId == null) {
            throw new IllegalArgumentException("`accManifestId` must not be null.");
        }

        AccManifestTagRecord accManifestTagRecord = new AccManifestTagRecord();
        accManifestTagRecord.setAccManifestId(valueOf(accManifestId));
        accManifestTagRecord.setTagId(valueOf(tagId));
        accManifestTagRecord.setCreatedBy(valueOf(requester().userId()));
        accManifestTagRecord.setCreationTimestamp(LocalDateTime.now());
        int numOfInsertedRecords = dslContext().insertInto(ACC_MANIFEST_TAG)
                .set(accManifestTagRecord)
                .execute();
        return numOfInsertedRecords == 1;
    }

    @Override
    public boolean removeTag(TagId tagId, AccManifestId accManifestId) {

        if (tagId == null) {
            throw new IllegalArgumentException("`tagId` must not be null.");
        }
        if (accManifestId == null) {
            throw new IllegalArgumentException("`accManifestId` must not be null.");
        }

        int numOfDeletedRecords = dslContext().deleteFrom(ACC_MANIFEST_TAG)
                .where(and(
                        ACC_MANIFEST_TAG.ACC_MANIFEST_ID.eq(valueOf(accManifestId)),
                        ACC_MANIFEST_TAG.TAG_ID.eq(valueOf(tagId))
                ))
                .execute();
        return numOfDeletedRecords == 1;
    }

    @Override
    public boolean addTag(TagId tagId, AsccpManifestId asccpManifestId) {

        if (tagId == null) {
            throw new IllegalArgumentException("`tagId` must not be null.");
        }
        if (asccpManifestId == null) {
            throw new IllegalArgumentException("`asccpManifestId` must not be null.");
        }

        AsccpManifestTagRecord asccpManifestTagRecord = new AsccpManifestTagRecord();
        asccpManifestTagRecord.setAsccpManifestId(valueOf(asccpManifestId));
        asccpManifestTagRecord.setTagId(valueOf(tagId));
        asccpManifestTagRecord.setCreatedBy(valueOf(requester().userId()));
        asccpManifestTagRecord.setCreationTimestamp(LocalDateTime.now());
        int numOfInsertedRecords = dslContext().insertInto(ASCCP_MANIFEST_TAG)
                .set(asccpManifestTagRecord)
                .execute();
        return numOfInsertedRecords == 1;
    }

    @Override
    public boolean removeTag(TagId tagId, AsccpManifestId asccpManifestId) {

        if (tagId == null) {
            throw new IllegalArgumentException("`tagId` must not be null.");
        }
        if (asccpManifestId == null) {
            throw new IllegalArgumentException("`asccpManifestId` must not be null.");
        }

        int numOfDeletedRecords = dslContext().deleteFrom(ASCCP_MANIFEST_TAG)
                .where(and(
                        ASCCP_MANIFEST_TAG.ASCCP_MANIFEST_ID.eq(valueOf(asccpManifestId)),
                        ASCCP_MANIFEST_TAG.TAG_ID.eq(valueOf(tagId))
                ))
                .execute();
        return numOfDeletedRecords == 1;
    }

    @Override
    public boolean addTag(TagId tagId, BccpManifestId bccpManifestId) {

        if (tagId == null) {
            throw new IllegalArgumentException("`tagId` must not be null.");
        }
        if (bccpManifestId == null) {
            throw new IllegalArgumentException("`bccpManifestId` must not be null.");
        }

        BccpManifestTagRecord bccpManifestTagRecord = new BccpManifestTagRecord();
        bccpManifestTagRecord.setBccpManifestId(valueOf(bccpManifestId));
        bccpManifestTagRecord.setTagId(valueOf(tagId));
        bccpManifestTagRecord.setCreatedBy(valueOf(requester().userId()));
        bccpManifestTagRecord.setCreationTimestamp(LocalDateTime.now());
        int numOfInsertedRecords = dslContext().insertInto(BCCP_MANIFEST_TAG)
                .set(bccpManifestTagRecord)
                .execute();
        return numOfInsertedRecords == 1;
    }

    @Override
    public boolean removeTag(TagId tagId, BccpManifestId bccpManifestId) {

        if (tagId == null) {
            throw new IllegalArgumentException("`tagId` must not be null.");
        }
        if (bccpManifestId == null) {
            throw new IllegalArgumentException("`bccpManifestId` must not be null.");
        }

        int numOfDeletedRecords = dslContext().deleteFrom(BCCP_MANIFEST_TAG)
                .where(and(
                        BCCP_MANIFEST_TAG.BCCP_MANIFEST_ID.eq(valueOf(bccpManifestId)),
                        BCCP_MANIFEST_TAG.TAG_ID.eq(valueOf(tagId))
                ))
                .execute();
        return numOfDeletedRecords == 1;
    }

    @Override
    public boolean addTag(TagId tagId, DtManifestId dtManifestId) {

        if (tagId == null) {
            throw new IllegalArgumentException("`tagId` must not be null.");
        }
        if (dtManifestId == null) {
            throw new IllegalArgumentException("`dtManifestId` must not be null.");
        }

        DtManifestTagRecord dtManifestTagRecord = new DtManifestTagRecord();
        dtManifestTagRecord.setDtManifestId(valueOf(dtManifestId));
        dtManifestTagRecord.setTagId(valueOf(tagId));
        dtManifestTagRecord.setCreatedBy(valueOf(requester().userId()));
        dtManifestTagRecord.setCreationTimestamp(LocalDateTime.now());
        int numOfInsertedRecords = dslContext().insertInto(DT_MANIFEST_TAG)
                .set(dtManifestTagRecord)
                .execute();
        return numOfInsertedRecords == 1;
    }

    @Override
    public boolean removeTag(TagId tagId, DtManifestId dtManifestId) {

        if (tagId == null) {
            throw new IllegalArgumentException("`tagId` must not be null.");
        }
        if (dtManifestId == null) {
            throw new IllegalArgumentException("`dtManifestId` must not be null.");
        }

        int numOfDeletedRecords = dslContext().deleteFrom(DT_MANIFEST_TAG)
                .where(and(
                        DT_MANIFEST_TAG.DT_MANIFEST_ID.eq(valueOf(dtManifestId)),
                        DT_MANIFEST_TAG.TAG_ID.eq(valueOf(tagId))
                ))
                .execute();
        return numOfDeletedRecords == 1;
    }
}
