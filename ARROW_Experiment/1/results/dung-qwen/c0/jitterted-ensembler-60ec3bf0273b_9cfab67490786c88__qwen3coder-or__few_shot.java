package com.jitterted.mobreg.domain;

import org.junit.jupiter.api.Test;

import java.net.URI;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class Ensemble_ArrowTest {

    @Test
    public void acceptedCount_InitiallyIsZero() {
        Ensemble ensemble = new Ensemble("Test", ZonedDateTime.now());

        assertThat(ensemble.acceptedCount()).isEqualTo(0);
    }

    @Test
    public void acceptedMembers_InitiallyIsEmpty() {
        Ensemble ensemble = new Ensemble("Test", ZonedDateTime.now());

        assertThat(ensemble.acceptedMembers().collect(Collectors.toList())).isEmpty();
    }

    @Test
    public void acceptedBy_AddsMemberToAcceptedList() {
        Ensemble ensemble = new Ensemble("Test", ZonedDateTime.now());
        MemberId memberId = new MemberId(1L);

        ensemble.acceptedBy(memberId);

        assertThat(ensemble.acceptedCount()).isEqualTo(1);
        assertThat(ensemble.acceptedMembers().collect(Collectors.toList())).containsExactly(memberId);
    }

    @Test
    public void acceptedBy_WhenEnsembleIsFull_ThrowsEnsembleFullException() {
        Ensemble ensemble = new Ensemble("Test", ZonedDateTime.now());
        MemberId memberId1 = new MemberId(1L);
        MemberId memberId2 = new MemberId(2L);
        MemberId memberId3 = new MemberId(3L);
        MemberId memberId4 = new MemberId(4L);
        MemberId memberId5 = new MemberId(5L);
        MemberId memberId6 = new MemberId(6L);

        ensemble.acceptedBy(memberId1);
        ensemble.acceptedBy(memberId2);
        ensemble.acceptedBy(memberId3);
        ensemble.acceptedBy(memberId4);
        ensemble.acceptedBy(memberId5);

        assertThatThrownBy(() -> ensemble.acceptedBy(memberId6))
                .isInstanceOf(EnsembleFullException.class);
    }

    @Test
    public void declinedBy_MemberIsAddedToDeclinedList() {
        Ensemble ensemble = new Ensemble("Test", ZonedDateTime.now());
        MemberId memberId = new MemberId(1L);

        ensemble.declinedBy(memberId);

        assertThat(ensemble.isDeclined(memberId)).isTrue();
        assertThat(ensemble.declinedCount()).isEqualTo(1);
    }

    @Test
    public void declinedBy_RemovesMemberFromAcceptedList() {
        Ensemble ensemble = new Ensemble("Test", ZonedDateTime.now());
        MemberId memberId = new MemberId(1L);

        ensemble.acceptedBy(memberId);
        ensemble.declinedBy(memberId);

        assertThat(ensemble.isAccepted(memberId)).isFalse();
        assertThat(ensemble.isDeclined(memberId)).isTrue();
    }

    @Test
    public void declinedMembers_InitiallyIsEmpty() {
        Ensemble ensemble = new Ensemble("Test", ZonedDateTime.now());

        assertThat(ensemble.declinedMembers().collect(Collectors.toList())).isEmpty();
    }

    @Test
    public void joinAsSpectator_AddsMemberToSpectators() {
        Ensemble ensemble = new Ensemble("Test", ZonedDateTime.now());
        MemberId memberId = new MemberId(1L);

        ensemble.joinAsSpectator(memberId);

        assertThat(ensemble.spectators()).containsExactly(memberId);
    }

    @Test
    public void joinAsSpectator_RemovesMemberFromAcceptedAndDeclined() {
        Ensemble ensemble = new Ensemble("Test", ZonedDateTime.now());
        MemberId memberId = new MemberId(1L);

        ensemble.acceptedBy(memberId);
        ensemble.joinAsSpectator(memberId);

        assertThat(ensemble.isAccepted(memberId)).isFalse();
        assertThat(ensemble.isDeclined(memberId)).isFalse();
        assertThat(ensemble.spectators()).containsExactly(memberId);
    }

    @Test
    public void canAccept_WhenEnsembleIsNotFull_ReturnsTrue() {
        Ensemble ensemble = new Ensemble("Test", ZonedDateTime.now());

        assertThat(ensemble.canAccept()).isTrue();
    }

    @Test
    public void canAccept_WhenEnsembleIsFull_ReturnsFalse() {
        Ensemble ensemble = new Ensemble("Test", ZonedDateTime.now());
        MemberId memberId1 = new MemberId(1L);
        MemberId memberId2 = new MemberId(2L);
        MemberId memberId3 = new MemberId(3L);
        MemberId memberId4 = new MemberId(4L);
        MemberId memberId5 = new MemberId(5L);

        ensemble.acceptedBy(memberId1);
        ensemble.acceptedBy(memberId2);
        ensemble.acceptedBy(memberId3);
        ensemble.acceptedBy(memberId4);
        ensemble.acceptedBy(memberId5);

        assertThat(ensemble.canAccept()).isFalse();
    }

    @Test
    public void isFull_WhenLessThanMax_ReturnsFalse() {
        Ensemble ensemble = new Ensemble("Test", ZonedDateTime.now());
        MemberId memberId1 = new MemberId(1L);
        MemberId memberId2 = new MemberId(2L);

        ensemble.acceptedBy(memberId1);
        ensemble.acceptedBy(memberId2);

        assertThat(ensemble.isFull()).isFalse();
    }

    @Test
    public void isFull_WhenEqualToMax_ReturnsTrue() {
        Ensemble ensemble = new Ensemble("Test", ZonedDateTime.now());
        MemberId memberId1 = new MemberId(1L);
        MemberId memberId2 = new MemberId(2L);
        MemberId memberId3 = new MemberId(3L);
        MemberId memberId4 = new MemberId(4L);
        MemberId memberId5 = new MemberId(5L);

        ensemble.acceptedBy(memberId1);
        ensemble.acceptedBy(memberId2);
        ensemble.acceptedBy(memberId3);
        ensemble.acceptedBy(memberId4);
        ensemble.acceptedBy(memberId5);

        assertThat(ensemble.isFull()).isTrue();
    }

    @Test
    public void isAccepted_WhenMemberHasNotAccepted_ReturnsFalse() {
        Ensemble ensemble = new Ensemble("Test", ZonedDateTime.now());
        MemberId memberId = new MemberId(1L);

        assertThat(ensemble.isAccepted(memberId)).isFalse();
    }

    @Test
    public void isAccepted_WhenMemberHasAccepted_ReturnsTrue() {
        Ensemble ensemble = new Ensemble("Test", ZonedDateTime.now());
        MemberId memberId = new MemberId(1L);

        ensemble.acceptedBy(memberId);

        assertThat(ensemble.isAccepted(memberId)).isTrue();
    }

    @Test
    public void meetingLink_InitiallyReturnsEmptyUri() {
        Ensemble ensemble = new Ensemble("Test", ZonedDateTime.now());

        assertThat(ensemble.meetingLink()).isEqualTo(URI.create(""));
    }

    @Test
    public void changeMeetingLinkTo_ChangesMeetingLink() {
        Ensemble ensemble = new Ensemble("Test", ZonedDateTime.now());
        URI newLink = URI.create("https://zoom.us/test");

        ensemble.changeMeetingLinkTo(newLink);

        assertThat(ensemble.meetingLink()).isEqualTo(newLink);
    }

    @Test
    public void complete_MarksEnsembleAsCompleted() {
        Ensemble ensemble = new Ensemble("Test", ZonedDateTime.now());

        ensemble.complete();

        assertThat(ensemble.isCompleted()).isTrue();
    }

    @Test
    public void complete_WhenEnsembleIsCanceled_ThrowsEnsembleCanceledException() {
        Ensemble ensemble = new Ensemble("Test", ZonedDateTime.now());
        ensemble.cancel();

        assertThatThrownBy(ensemble::complete)
                .isInstanceOf(EnsembleCanceled.class);
    }

    @Test
    public void isCompleted_InitiallyReturnsFalse() {
        Ensemble ensemble = new Ensemble("Test", ZonedDateTime.now());

        assertThat(ensemble.isCompleted()).isFalse();
    }

    @Test
    public void linkToRecordingAt_SetsRecordingLink() {
        Ensemble ensemble = new Ensemble("Test", ZonedDateTime.now());
        URI recordingLink = URI.create("https://example.com/recording");

        ensemble.linkToRecordingAt(recordingLink);

        assertThat(ensemble.recordingLink()).isEqualTo(recordingLink);
    }

    @Test
    public void recordingLink_InitiallyReturnsEmptyUri() {
        Ensemble ensemble = new Ensemble("Test", ZonedDateTime.now());

        assertThat(ensemble.recordingLink()).isEqualTo(URI.create(""));
    }

    @Test
    public void changeNameTo_ChangesName() {
        Ensemble ensemble = new Ensemble("Test", ZonedDateTime.now());

        ensemble.changeNameTo("New Name");

        assertThat(ensemble.name()).isEqualTo("New Name");
    }

    @Test
    public void changeNameTo_WhenNameIsNull_ThrowsIllegalArgumentException() {
        Ensemble ensemble = new Ensemble("Test", ZonedDateTime.now());

        assertThatThrownBy(() -> ensemble.changeNameTo(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void changeStartDateTimeTo_ChangesStartDateTime() {
        Ensemble ensemble = new Ensemble("Test", ZonedDateTime.now());
        ZonedDateTime newDateTime = ZonedDateTime.now().plusDays(1);

        ensemble.changeStartDateTimeTo(newDateTime);

        assertThat(ensemble.startDateTime()).isEqualTo(newDateTime);
    }

    @Test
    public void changeStartDateTimeTo_WhenDateTimeIsNull_ThrowsIllegalArgumentException() {
        Ensemble ensemble = new Ensemble("Test", ZonedDateTime.now());

        assertThatThrownBy(() -> ensemble.changeStartDateTimeTo(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void statusFor_WhenMemberHasNotRsvped_ReturnsUnknown() {
        Ensemble ensemble = new Ensemble("Test", ZonedDateTime.now());
        MemberId memberId = new MemberId(1L);
        ZonedDateTime now = ZonedDateTime.now();

        MemberStatus status = ensemble.statusFor(memberId, now);

        assertThat(status).isEqualTo(MemberStatus.UNKNOWN);
    }

    @Test
    public void statusFor_WhenMemberHasAccepted_ReturnsAccepted() {
        Ensemble ensemble = new Ensemble("Test", ZonedDateTime.now());
        MemberId memberId = new MemberId(1L);
        ZonedDateTime now = ZonedDateTime.now();

        ensemble.acceptedBy(memberId);
        MemberStatus status = ensemble.statusFor(memberId, now);

        assertThat(status).isEqualTo(MemberStatus.ACCEPTED);
    }

    @Test
    public void statusFor_WhenMemberHasDeclined_ReturnsDeclined() {
        Ensemble ensemble = new Ensemble("Test", ZonedDateTime.now());
        MemberId memberId = new MemberId(1L);
        ZonedDateTime now = ZonedDateTime.now();

        ensemble.declinedBy(memberId);
        MemberStatus status = ensemble.statusFor(memberId, now);

        assertThat(status).isEqualTo(MemberStatus.DECLINED);
    }

    @Test
    public void statusFor_WhenEnsembleIsCanceled_ReturnsCanceled() {
        Ensemble ensemble = new Ensemble("Test", ZonedDateTime.now());
        MemberId memberId = new MemberId(1L);
        ZonedDateTime now = ZonedDateTime.now();

        ensemble.acceptedBy(memberId);
        ensemble.cancel();
        MemberStatus status = ensemble.statusFor(memberId, now);

        assertThat(status).isEqualTo(MemberStatus.CANCELED);
    }

    @Test
    public void statusFor_WhenEnsembleIsCanceledButMemberNotAccepted_ReturnsHidden() {
        Ensemble ensemble = new Ensemble("Test", ZonedDateTime.now());
        MemberId memberId = new MemberId(1L);
        ZonedDateTime now = ZonedDateTime.now();

        ensemble.cancel();
        MemberStatus status = ensemble.statusFor(memberId, now);

        assertThat(status).isEqualTo(MemberStatus.HIDDEN);
    }

    @Test
    public void rsvpOf_WhenMemberHasNotRsvped_ReturnsUnknown() {
        Ensemble ensemble = new Ensemble("Test", ZonedDateTime.now());
        MemberId memberId = new MemberId(1L);

        Rsvp rsvp = ensemble.rsvpOf(memberId);

        assertThat(rsvp).isEqualTo(Rsvp.UNKNOWN);
    }

    @Test
    public void rsvpOf_WhenMemberHasAccepted_ReturnsAccepted() {
        Ensemble ensemble = new Ensemble("Test", ZonedDateTime.now());
        MemberId memberId = new MemberId(1L);

        ensemble.acceptedBy(memberId);
        Rsvp rsvp = ensemble.rsvpOf(memberId);

        assertThat(rsvp).isEqualTo(Rsvp.ACCEPTED);
    }

    @Test
    public void rsvpOf_WhenMemberHasDeclined_ReturnsDeclined() {
        Ensemble ensemble = new Ensemble("Test", ZonedDateTime.now());
        MemberId memberId = new MemberId(1L);

        ensemble.declinedBy(memberId);
        Rsvp rsvp = ensemble.rsvpOf(memberId);

        assertThat(rsvp).isEqualTo(Rsvp.DECLINED);
    }

    @Test
    public void declinedCount_InitiallyIsZero() {
        Ensemble ensemble = new Ensemble("Test", ZonedDateTime.now());

        assertThat(ensemble.declinedCount()).isEqualTo(0);
    }

    @Test
    public void isCanceled_InitiallyReturnsFalse() {
        Ensemble ensemble = new Ensemble("Test", ZonedDateTime.now());

        assertThat(ensemble.isCanceled()).isFalse();
    }

    @Test
    public void cancel_MarksEnsembleAsCanceled() {
        Ensemble ensemble = new Ensemble("Test", ZonedDateTime.now());

        ensemble.cancel();

        assertThat(ensemble.isCanceled()).isTrue();
    }

    @Test
    public void cancel_WhenEnsembleIsCompleted_ThrowsEnsembleCompletedException() {
        Ensemble ensemble = new Ensemble("Test", ZonedDateTime.now());
        ensemble.complete();

        assertThatThrownBy(ensemble::cancel)
                .isInstanceOf(EnsembleCompleted.class);
    }

    @Test
    public void state_InitiallyReturnsScheduled() {
        Ensemble ensemble = new Ensemble("Test", ZonedDateTime.now());

        assertThat(ensemble.state()).isEqualTo(EnsembleState.SCHEDULED);
    }

    @Test
    public void state_WhenCanceled_ReturnsCanceled() {
        Ensemble ensemble = new Ensemble("Test", ZonedDateTime.now());
        ensemble.cancel();

        assertThat(ensemble.state()).isEqualTo(EnsembleState.CANCELED);
    }

    @Test
    public void state_WhenCompleted_ReturnsCompleted() {
        Ensemble ensemble = new Ensemble("Test", ZonedDateTime.now());
        ensemble.complete();

        assertThat(ensemble.state()).isEqualTo(EnsembleState.COMPLETED);
    }

    @Test
    public void conferenceDetails_InitiallyReturnsDefaultConferenceDetails() {
        Ensemble ensemble = new Ensemble("Test", ZonedDateTime.now());

        assertThat(ensemble.conferenceDetails().name()).isEqualTo("");
        assertThat(ensemble.conferenceDetails().joinUrl()).isEqualTo(URI.create(""));
    }

    @Test
    public void changeConferenceDetailsTo_ChangesConferenceDetails() {
        Ensemble ensemble = new Ensemble("Test", ZonedDateTime.now());
        ConferenceDetails newDetails = new ConferenceDetails("New Conference", URI.create(""), URI.create("https://zoom.us/new"));

        ensemble.changeConferenceDetailsTo(newDetails);

        assertThat(ensemble.conferenceDetails()).isEqualTo(newDetails);
    }

    @Test
    public void removeConferenceDetails_SetsConferenceDetailsToDeleted() {
        Ensemble ensemble = new Ensemble("Test", ZonedDateTime.now());

        ensemble.removeConferenceDetails();

        assertThat(ensemble.conferenceDetails()).isEqualTo(ConferenceDetails.DELETED);
    }

}
