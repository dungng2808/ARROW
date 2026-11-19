package com.jitterted.mobreg.domain;

import org.junit.Test;

import java.net.URI;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class Ensemble_ArrowTest {

    @Test
    public void nameReturnsSetName() {
        Ensemble ensemble = new Ensemble("Test Ensemble", ZonedDateTime.now());
        assertThat(ensemble.name()).isEqualTo("Test Ensemble");
    }

    @Test
    public void startDateTimeReturnsSetStartDateTime() {
        ZonedDateTime start = ZonedDateTime.now().plusDays(1);
        Ensemble ensemble = new Ensemble("Test", start);
        assertThat(ensemble.startDateTime()).isEqualTo(start);
    }

    @Test
    public void acceptedCountInitiallyZero() {
        Ensemble ensemble = new Ensemble("Test", ZonedDateTime.now());
        assertThat(ensemble.acceptedCount()).isZero();
    }

    @Test
    public void acceptedMembersInitiallyEmpty() {
        Ensemble ensemble = new Ensemble("Test", ZonedDateTime.now());
        assertThat(ensemble.acceptedMembers().collect(Collectors.toList())).isEmpty();
    }

    @Test
    public void acceptedByAddsMemberToAcceptedMembers() {
        Ensemble ensemble = new Ensemble("Test", ZonedDateTime.now());
        MemberId memberId = new MemberId(1L);
        ensemble.acceptedBy(memberId);
        assertThat(ensemble.acceptedCount()).isEqualTo(1);
        assertThat(ensemble.acceptedMembers().collect(Collectors.toList())).containsExactly(memberId);
    }

    @Test
    public void acceptedByTwiceDoesNotAddDuplicate() {
        Ensemble ensemble = new Ensemble("Test", ZonedDateTime.now());
        MemberId memberId = new MemberId(1L);
        ensemble.acceptedBy(memberId);
        ensemble.acceptedBy(memberId);
        assertThat(ensemble.acceptedCount()).isEqualTo(1);
    }

    @Test
    public void declinedByRemovesFromAcceptedAndAddsToDeclined() {
        Ensemble ensemble = new Ensemble("Test", ZonedDateTime.now());
        MemberId memberId = new MemberId(1L);
        ensemble.acceptedBy(memberId);
        ensemble.declinedBy(memberId);
        assertThat(ensemble.acceptedCount()).isZero();
        assertThat(ensemble.declinedCount()).isEqualTo(1);
        assertThat(ensemble.isDeclined(memberId)).isTrue();
        assertThat(ensemble.isAccepted(memberId)).isFalse();
    }

    @Test
    public void declinedMembersInitiallyEmpty() {
        Ensemble ensemble = new Ensemble("Test", ZonedDateTime.now());
        assertThat(ensemble.declinedMembers().collect(Collectors.toList())).isEmpty();
    }

    @Test
    public void declinedByAddsToDeclinedMembers() {
        Ensemble ensemble = new Ensemble("Test", ZonedDateTime.now());
        MemberId memberId = new MemberId(1L);
        ensemble.declinedBy(memberId);
        assertThat(ensemble.declinedMembers().collect(Collectors.toList())).containsExactly(memberId);
    }

    @Test
    public void canAcceptReturnsTrueWhenNotFull() {
        Ensemble ensemble = new Ensemble("Test", ZonedDateTime.now());
        assertThat(ensemble.canAccept()).isTrue();
    }

    @Test
    public void canAcceptReturnsFalseWhenFull() {
        Ensemble ensemble = new Ensemble("Test", ZonedDateTime.now());
        for (int i = 0; i < 5; i++) {
            ensemble.acceptedBy(new MemberId(i + 1));
        }
        assertThat(ensemble.canAccept()).isFalse();
    }

    @Test
    public void isFullReturnsTrueWhenFiveAccepted() {
        Ensemble ensemble = new Ensemble("Test", ZonedDateTime.now());
        for (int i = 0; i < 5; i++) {
            ensemble.acceptedBy(new MemberId(i + 1));
        }
        assertThat(ensemble.isFull()).isTrue();
    }

    @Test
    public void isFullReturnsFalseWhenLessThanFiveAccepted() {
        Ensemble ensemble = new Ensemble("Test", ZonedDateTime.now());
        ensemble.acceptedBy(new MemberId(1L));
        assertThat(ensemble.isFull()).isFalse();
    }

    @Test
    public void isAcceptedReturnsTrueWhenMemberIsAccepted() {
        Ensemble ensemble = new Ensemble("Test", ZonedDateTime.now());
        MemberId memberId = new MemberId(1L);
        ensemble.acceptedBy(memberId);
        assertThat(ensemble.isAccepted(memberId)).isTrue();
    }

    @Test
    public void isAcceptedReturnsFalseWhenMemberNotAccepted() {
        Ensemble ensemble = new Ensemble("Test", ZonedDateTime.now());
        MemberId memberId = new MemberId(1L);
        assertThat(ensemble.isAccepted(memberId)).isFalse();
    }

    @Test
    public void meetingLinkReturnsConferenceDetailsJoinUrl() {
        URI meetingLink = URI.create("https://zoom.us/j/123456789");
        Ensemble ensemble = new Ensemble("Test", meetingLink, ZonedDateTime.now());
        assertThat(ensemble.meetingLink()).isEqualTo(meetingLink);
    }

    @Test
    public void changeMeetingLinkToUpdatesMeetingLink() {
        Ensemble ensemble = new Ensemble("Test", ZonedDateTime.now());
        URI newLink = URI.create("https://zoom.us/j/987654321");
        ensemble.changeMeetingLinkTo(newLink);
        assertThat(ensemble.meetingLink()).isEqualTo(newLink);
    }

    @Test
    public void completeSetsStateToCompleted() {
        Ensemble ensemble = new Ensemble("Test", ZonedDateTime.now());
        ensemble.complete();
        assertThat(ensemble.isCompleted()).isTrue();
        assertThat(ensemble.state()).isEqualTo(EnsembleState.COMPLETED);
    }

    @Test
    public void isCompletedReturnsTrueWhenCompleted() {
        Ensemble ensemble = new Ensemble("Test", ZonedDateTime.now());
        ensemble.complete();
        assertThat(ensemble.isCompleted()).isTrue();
    }

    @Test
    public void isCompletedReturnsFalseWhenNotCompleted() {
        Ensemble ensemble = new Ensemble("Test", ZonedDateTime.now());
        assertThat(ensemble.isCompleted()).isFalse();
    }

    @Test
    public void linkToRecordingAtSetsRecordingLink() {
        Ensemble ensemble = new Ensemble("Test", ZonedDateTime.now());
        URI recordingLink = URI.create("https://example.com/recording.mp4");
        ensemble.linkToRecordingAt(recordingLink);
        assertThat(ensemble.recordingLink()).isEqualTo(recordingLink);
    }

    @Test
    public void recordingLinkReturnsInitialEmptyString() {
        Ensemble ensemble = new Ensemble("Test", ZonedDateTime.now());
        assertThat(ensemble.recordingLink().toString()).isEmpty();
    }

    @Test
    public void changeNameToUpdatesName() {
        Ensemble ensemble = new Ensemble("Old Name", ZonedDateTime.now());
        ensemble.changeNameTo("New Name");
        assertThat(ensemble.name()).isEqualTo("New Name");
    }

    @Test
    public void changeNameToNullThrowsIllegalArgumentException() {
        Ensemble ensemble = new Ensemble("Test", ZonedDateTime.now());
        assertThatThrownBy(() -> ensemble.changeNameTo(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void changeStartDateTimeToUpdatesStartDateTime() {
        Ensemble ensemble = new Ensemble("Test", ZonedDateTime.now());
        ZonedDateTime newStart = ZonedDateTime.now().plusDays(1);
        ensemble.changeStartDateTimeTo(newStart);
        assertThat(ensemble.startDateTime()).isEqualTo(newStart);
    }

    @Test
    public void changeStartDateTimeToNullThrowsIllegalArgumentException() {
        Ensemble ensemble = new Ensemble("Test", ZonedDateTime.now());
        assertThatThrownBy(() -> ensemble.changeStartDateTimeTo(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void joinAsSpectatorAddsToSpectators() {
        Ensemble ensemble = new Ensemble("Test", ZonedDateTime.now());
        MemberId memberId = new MemberId(1L);
        ensemble.joinAsSpectator(memberId);
        assertThat(ensemble.spectators()).containsExactly(memberId);
    }

    @Test
    public void joinAsSpectatorRemovesFromAcceptedAndDeclined() {
        Ensemble ensemble = new Ensemble("Test", ZonedDateTime.now());
        MemberId memberId = new MemberId(1L);
        ensemble.acceptedBy(memberId);
        ensemble.joinAsSpectator(memberId);
        assertThat(ensemble.isAccepted(memberId)).isFalse();
        assertThat(ensemble.isDeclined(memberId)).isFalse();
        assertThat(ensemble.spectators()).containsExactly(memberId);
    }

    @Test
    public void rsvpOfReturnsDeclinedWhenMemberDeclined() {
        Ensemble ensemble = new Ensemble("Test", ZonedDateTime.now());
        MemberId memberId = new MemberId(1L);
        ensemble.declinedBy(memberId);
        assertThat(ensemble.rsvpOf(memberId)).isEqualTo(Rsvp.DECLINED);
    }

    @Test
    public void rsvpOfReturnsAcceptedWhenMemberAccepted() {
        Ensemble ensemble = new Ensemble("Test", ZonedDateTime.now());
        MemberId memberId = new MemberId(1L);
        ensemble.acceptedBy(memberId);
        assertThat(ensemble.rsvpOf(memberId)).isEqualTo(Rsvp.ACCEPTED);
    }

    @Test
    public void rsvpOfReturnsUnknownWhenMemberNeitherAcceptedNorDeclined() {
        Ensemble ensemble = new Ensemble("Test", ZonedDateTime.now());
        MemberId memberId = new MemberId(1L);
        assertThat(ensemble.rsvpOf(memberId)).isEqualTo(Rsvp.UNKNOWN);
    }

    @Test
    public void cancelSetsStateToCanceled() {
        Ensemble ensemble = new Ensemble("Test", ZonedDateTime.now());
        ensemble.cancel();
        assertThat(ensemble.isCanceled()).isTrue();
        assertThat(ensemble.state()).isEqualTo(EnsembleState.CANCELED);
    }

    @Test
    public void isCanceledReturnsTrueWhenCanceled() {
        Ensemble ensemble = new Ensemble("Test", ZonedDateTime.now());
        ensemble.cancel();
        assertThat(ensemble.isCanceled()).isTrue();
    }

    @Test
    public void isCanceledReturnsFalseWhenNotCanceled() {
        Ensemble ensemble = new Ensemble("Test", ZonedDateTime.now());
        assertThat(ensemble.isCanceled()).isFalse();
    }

    @Test
    public void statusForReturnsCanceledWhenMemberAcceptedAndEnsembleCanceled() {
        Ensemble ensemble = new Ensemble("Test", ZonedDateTime.now());
        MemberId memberId = new MemberId(1L);
        ensemble.acceptedBy(memberId);
        ensemble.cancel();
        ZonedDateTime now = ZonedDateTime.now();
        assertThat(ensemble.statusFor(memberId, now)).isEqualTo(MemberStatus.CANCELED);
    }

    @Test
    public void statusForReturnsHiddenWhenEnsembleCanceled() {
        Ensemble ensemble = new Ensemble("Test", ZonedDateTime.now());
        ensemble.cancel();
        MemberId memberId = new MemberId(1L);
        ZonedDateTime now = ZonedDateTime.now();
        assertThat(ensemble.statusFor(memberId, now)).isEqualTo(MemberStatus.HIDDEN);
    }

    @Test
    public void statusForReturnsInGracePeriodWhenInGracePeriod() {
        Ensemble ensemble = new Ensemble("Test", ZonedDateTime.now());
        MemberId memberId = new MemberId(1L);
        ZonedDateTime now = ensemble.startDateTime().plus(Duration.ofMinutes(5));
        assertThat(ensemble.statusFor(memberId, now)).isEqualTo(MemberStatus.IN_GRACE_PERIOD);
    }

    @Test
    public void statusForReturnsHiddenWhenInProgressButBeforeGracePeriodEnd() {
        Ensemble ensemble = new Ensemble("Test", ZonedDateTime.now());
        MemberId memberId = new MemberId(1L);
        ensemble.acceptedBy(memberId);
        ZonedDateTime now = ensemble.startDateTime().plus(Duration.ofMinutes(15));
        assertThat(ensemble.statusFor(memberId, now)).isEqualTo(MemberStatus.HIDDEN);
    }

    @Test
    public void statusForReturnsAcceptedWhenFutureAndAvailableAndAccepted() {
        Ensemble ensemble = new Ensemble("Test", ZonedDateTime.now().plusDays(1));
        MemberId memberId = new MemberId(1L);
        ensemble.acceptedBy(memberId);
        ZonedDateTime now = ZonedDateTime.now();
        assertThat(ensemble.statusFor(memberId, now)).isEqualTo(MemberStatus.ACCEPTED);
    }

    @Test
    public void statusForReturnsFullWhenFutureAndFullAndUnknown() {
        Ensemble ensemble = new Ensemble("Test", ZonedDateTime.now().plusDays(1));
        for (int i = 0; i < 5; i++) {
            ensemble.acceptedBy(new MemberId(i + 1));
        }
        MemberId memberId = new MemberId(6L);
        ZonedDateTime now = ZonedDateTime.now();
        assertThat(ensemble.statusFor(memberId, now)).isEqualTo(MemberStatus.FULL);
    }

    @Test
    public void statusForReturnsDeclinedWhenFutureAndDeclined() {
        Ensemble ensemble = new Ensemble("Test", ZonedDateTime.now().plusDays(1));
        MemberId memberId = new MemberId(1L);
        ensemble.declinedBy(memberId);
        ZonedDateTime now = ZonedDateTime.now();
        assertThat(ensemble.statusFor(memberId, now)).isEqualTo(MemberStatus.DECLINED);
    }

    @Test
    public void statusForReturnsDeclinedFullWhenFutureAndFullAndDeclined() {
        Ensemble ensemble = new Ensemble("Test", ZonedDateTime.now().plusDays(1));
        for (int i = 0; i < 5; i++) {
            ensemble.acceptedBy(new MemberId(i + 1));
        }
        MemberId memberId = new MemberId(6L);
        ensemble.declinedBy(memberId);
        ZonedDateTime now = ZonedDateTime.now();
        assertThat(ensemble.statusFor(memberId, now)).isEqualTo(MemberStatus.DECLINED_FULL);
    }

    @Test
    public void conferenceDetailsReturnsConferenceDetails() {
        Ensemble ensemble = new Ensemble("Test", ZonedDateTime.now());
        assertThat(ensemble.conferenceDetails()).isNotNull();
    }

    @Test
    public void changeConferenceDetailsToUpdatesConferenceDetails() {
        Ensemble ensemble = new Ensemble("Test", ZonedDateTime.now());
        ConferenceDetails newDetails = new ConferenceDetails("New Meeting", URI.create("https://zoom.us/j/123"), URI.create("https://zoom.us/j/456"));
        ensemble.changeConferenceDetailsTo(newDetails);
        assertThat(ensemble.conferenceDetails()).isEqualTo(newDetails);
    }

    @Test
    public void removeConferenceDetailsSetsConferenceDetailsToDeleted() {
        Ensemble ensemble = new Ensemble("Test", ZonedDateTime.now());
        ensemble.removeConferenceDetails();
        assertThat(ensemble.conferenceDetails()).isEqualTo(ConferenceDetails.DELETED);
    }

    @Test
    public void acceptedByThrowsEnsembleFullExceptionWhenFull() {
        Ensemble ensemble = new Ensemble("Test", ZonedDateTime.now());
        for (int i = 0; i < 5; i++) {
            ensemble.acceptedBy(new MemberId(i + 1));
        }
        assertThatThrownBy(() -> ensemble.acceptedBy(new MemberId(6L)))
                .isInstanceOf(EnsembleFullException.class);
    }

    @Test
    public void acceptedByThrowsEnsembleCanceledExceptionWhenCanceled() {
        Ensemble ensemble = new Ensemble("Test", ZonedDateTime.now());
        ensemble.cancel();
        assertThatThrownBy(() -> ensemble.acceptedBy(new MemberId(1L)))
                .isInstanceOf(EnsembleCanceled.class);
    }

    @Test
    public void completeThrowsEnsembleCanceledExceptionWhenCanceled() {
        Ensemble ensemble = new Ensemble("Test", ZonedDateTime.now());
        ensemble.cancel();
        assertThatThrownBy(() -> ensemble.complete())
                .isInstanceOf(EnsembleCanceled.class);
    }

    @Test
    public void completeThrowsEnsembleCompletedExceptionWhenCompleted() {
        Ensemble ensemble = new Ensemble("Test", ZonedDateTime.now());
        ensemble.complete();
        assertThatThrownBy(() -> ensemble.complete())
                .isInstanceOf(EnsembleCompleted.class);
    }

    @Test
    public void cancelThrowsEnsembleCompletedExceptionWhenCompleted() {
        Ensemble ensemble = new Ensemble("Test", ZonedDateTime.now());
        ensemble.complete();
        assertThatThrownBy(() -> ensemble.cancel())
                .isInstanceOf(EnsembleCompleted.class);
    }

    @Test
    public void stateReturnsScheduledInitially() {
        Ensemble ensemble = new Ensemble("Test", ZonedDateTime.now());
        assertThat(ensemble.state()).isEqualTo(EnsembleState.SCHEDULED);
    }

    @Test
    public void stateReturnsCanceledWhenCanceled() {
        Ensemble ensemble = new Ensemble("Test", ZonedDateTime.now());
        ensemble.cancel();
        assertThat(ensemble.state()).isEqualTo(EnsembleState.CANCELED);
    }

    @Test
    public void stateReturnsCompletedWhenCompleted() {
        Ensemble ensemble = new Ensemble("Test", ZonedDateTime.now());
        ensemble.complete();
        assertThat(ensemble.state()).isEqualTo(EnsembleState.COMPLETED);
    }

    @Test
    public void spectatorsReturnsImmutableSet() {
        Ensemble ensemble = new Ensemble("Test", ZonedDateTime.now());
        MemberId memberId = new MemberId(1L);
        ensemble.joinAsSpectator(memberId);
        Set<MemberId> spectators = ensemble.spectators();
        assertThat(spectators).containsExactly(memberId);
        // Try to modify the returned set
        try {
            spectators.add(new MemberId(2L));
            assertThat(false).as("Should not be able to modify returned set").isTrue();
        } catch (UnsupportedOperationException e) {
            // Expected
        }
    }

    @Test
    public void acceptedMembersReturnsImmutableSet() {
        Ensemble ensemble = new Ensemble("Test", ZonedDateTime.now());
        MemberId memberId = new MemberId(1L);
        ensemble.acceptedBy(memberId);
        Set<MemberId> accepted = new HashSet<>();
        ensemble.acceptedMembers().forEach(accepted::add);
        assertThat(accepted).containsExactly(memberId);
        // Try to modify the returned stream
        try {
            accepted.add(new MemberId(2L));
            assertThat(false).as("Should not be able to modify returned set").isTrue();
        } catch (UnsupportedOperationException e) {
            // Expected
        }
    }

    @Test
    public void declinedMembersReturnsImmutableSet() {
        Ensemble ensemble = new Ensemble("Test", ZonedDateTime.now());
        MemberId memberId = new MemberId(1L);
        ensemble.declinedBy(memberId);
        Set<MemberId> declined = new HashSet<>();
        ensemble.declinedMembers().forEach(declined::add);
        assertThat(declined).containsExactly(memberId);
        // Try to modify the returned stream
        try {
            declined.add(new MemberId(2L));
            assertThat(false).as("Should not be able to modify returned set").isTrue();
        } catch (UnsupportedOperationException e) {
            // Expected
        }
    }

}
