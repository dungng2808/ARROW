package com.jitterted.mobreg.domain;

import org.junit.Test;

import java.net.URI;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.HashSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class Ensemble_ArrowTest {

    private ZonedDateTime time(String value) {
        return ZonedDateTime.parse(value);
    }

    @Test
    public void constructorSetsNameStartTimeAndDefaultMeetingLink() {
        ZonedDateTime start = time("2030-01-01T12:00:00Z");
        Ensemble ensemble = new Ensemble("Practice", start);

        assertEquals("Practice", ensemble.name());
        assertEquals(start, ensemble.startDateTime());
        assertEquals(URI.create("https://zoom.us"), ensemble.meetingLink());
        assertEquals(EnsembleState.SCHEDULED, ensemble.state());
        assertEquals(URI.create(""), ensemble.recordingLink());
    }

    @Test
    public void constructorAcceptsMeetingLink() {
        URI link = URI.create("https://example.com/meeting");
        Ensemble ensemble = new Ensemble("Practice", link, time("2030-01-01T12:00:00Z"));

        assertEquals(link, ensemble.meetingLink());
    }

    @Test
    public void idCanBeSetAndRetrieved() {
        Ensemble ensemble = new Ensemble("Practice", time("2030-01-01T12:00:00Z"));
        EnsembleId id = new EnsembleId();

        ensemble.setId(id);

        assertEquals(id, ensemble.getId());
    }

    @Test
    public void acceptingMemberUpdatesRsvpAndAcceptedMembers() {
        Ensemble ensemble = new Ensemble("Practice", time("2030-01-01T12:00:00Z"));
        MemberId member = new MemberId();

        ensemble.acceptedBy(member);

        assertTrue(ensemble.isAccepted(member));
        assertEquals(Rsvp.ACCEPTED, ensemble.rsvpOf(member));
        assertEquals(1, ensemble.acceptedCount());
        assertEquals(new HashSet<MemberId>(Arrays.asList(member)), new HashSet<MemberId>() {{
            ensemble.acceptedMembers().forEach(this::add);
        }});
    }

    @Test
    public void decliningMemberRemovesAcceptance() {
        Ensemble ensemble = new Ensemble("Practice", time("2030-01-01T12:00:00Z"));
        MemberId member = new MemberId();
        ensemble.acceptedBy(member);

        ensemble.declinedBy(member);

        assertFalse(ensemble.isAccepted(member));
        assertTrue(ensemble.isDeclined(member));
        assertEquals(Rsvp.DECLINED, ensemble.rsvpOf(member));
        assertEquals(0, ensemble.acceptedCount());
        assertEquals(1, ensemble.declinedCount());
    }

    @Test
    public void acceptingAgainRemovesDeclinedStatus() {
        Ensemble ensemble = new Ensemble("Practice", time("2030-01-01T12:00:00Z"));
        MemberId member = new MemberId();
        ensemble.declinedBy(member);

        ensemble.acceptedBy(member);

        assertTrue(ensemble.isAccepted(member));
        assertFalse(ensemble.isDeclined(member));
        assertEquals(Rsvp.ACCEPTED, ensemble.rsvpOf(member));
        assertEquals(0, ensemble.declinedCount());
    }

    @Test
    public void joiningAsSpectatorRemovesRsvpAndAddsSpectator() {
        Ensemble ensemble = new Ensemble("Practice", time("2030-01-01T12:00:00Z"));
        MemberId member = new MemberId();
        ensemble.acceptedBy(member);

        ensemble.joinAsSpectator(member);

        assertTrue(ensemble.spectators().contains(member));
        assertFalse(ensemble.isAccepted(member));
        assertEquals(Rsvp.UNKNOWN, ensemble.rsvpOf(member));
    }

    @Test
    public void canAcceptUntilFiveMembersHaveAccepted() {
        Ensemble ensemble = new Ensemble("Practice", time("2030-01-01T12:00:00Z"));

        assertTrue(ensemble.canAccept());
        for (int i = 0; i < 5; i++) {
            ensemble.acceptedBy(new MemberId());
        }

        assertEquals(5, ensemble.acceptedCount());
        assertFalse(ensemble.canAccept());
    }

    @Test(expected = EnsembleFullException.class)
    public void cannotAcceptSixthMember() {
        Ensemble ensemble = new Ensemble("Practice", time("2030-01-01T12:00:00Z"));
        for (int i = 0; i < 5; i++) {
            ensemble.acceptedBy(new MemberId());
        }

        ensemble.acceptedBy(new MemberId());
    }

    @Test
    public void meetingLinkCanBeChanged() {
        Ensemble ensemble = new Ensemble("Practice", time("2030-01-01T12:00:00Z"));
        URI link = URI.create("https://example.com/new");

        ensemble.changeMeetingLinkTo(link);

        assertEquals(link, ensemble.meetingLink());
    }

    @Test
    public void recordingLinkCanBeChanged() {
        Ensemble ensemble = new Ensemble("Practice", time("2030-01-01T12:00:00Z"));
        URI link = URI.create("https://example.com/recording");

        ensemble.linkToRecordingAt(link);

        assertEquals(link, ensemble.recordingLink());
    }

    @Test
    public void completeChangesState() {
        Ensemble ensemble = new Ensemble("Practice", time("2030-01-01T12:00:00Z"));

        ensemble.complete();

        assertTrue(ensemble.isCompleted());
        assertEquals(EnsembleState.COMPLETED, ensemble.state());
    }

    @Test(expected = EnsembleCompleted.class)
    public void cannotAcceptAfterCompletion() {
        Ensemble ensemble = new Ensemble("Practice", time("2030-01-01T12:00:00Z"));
        ensemble.complete();

        ensemble.acceptedBy(new MemberId());
    }

    @Test
    public void cancelChangesState() {
        Ensemble ensemble = new Ensemble("Practice", time("2030-01-01T12:00:00Z"));

        ensemble.cancel();

        assertTrue(ensemble.isCanceled());
        assertEquals(EnsembleState.CANCELED, ensemble.state());
    }

    @Test(expected = EnsembleCanceled.class)
    public void cannotAcceptAfterCancellation() {
        Ensemble ensemble = new Ensemble("Practice", time("2030-01-01T12:00:00Z"));
        ensemble.cancel();

        ensemble.acceptedBy(new MemberId());
    }

    @Test(expected = EnsembleCompleted.class)
    public void cannotCancelAfterCompletion() {
        Ensemble ensemble = new Ensemble("Practice", time("2030-01-01T12:00:00Z"));
        ensemble.complete();

        ensemble.cancel();
    }

    @Test(expected = IllegalArgumentException.class)
    public void changingNameToNullIsRejected() {
        Ensemble ensemble = new Ensemble("Practice", time("2030-01-01T12:00:00Z"));

        ensemble.changeNameTo(null);
    }

    @Test
    public void nameCanBeChanged() {
        Ensemble ensemble = new Ensemble("Practice", time("2030-01-01T12:00:00Z"));

        ensemble.changeNameTo("New name");

        assertEquals("New name", ensemble.name());
    }

    @Test(expected = IllegalArgumentException.class)
    public void changingStartTimeToNullIsRejected() {
        Ensemble ensemble = new Ensemble("Practice", time("2030-01-01T12:00:00Z"));

        ensemble.changeStartDateTimeTo(null);
    }

    @Test
    public void startTimeCanBeChanged() {
        Ensemble ensemble = new Ensemble("Practice", time("2030-01-01T12:00:00Z"));
        ZonedDateTime newStart = time("2030-02-01T12:00:00Z");

        ensemble.changeStartDateTimeTo(newStart);

        assertEquals(newStart, ensemble.startDateTime());
    }

    @Test
    public void conferenceDetailsCanBeChangedAndRemoved() {
        Ensemble ensemble = new Ensemble("Practice", time("2030-01-01T12:00:00Z"));
        ConferenceDetails details = ensemble.conferenceDetails();

        ensemble.changeConferenceDetailsTo(details);

        assertEquals(details, ensemble.conferenceDetails());

        ensemble.removeConferenceDetails();

        assertEquals(ConferenceDetails.DELETED, ensemble.conferenceDetails());
    }

    @Test
    public void statusForFutureUnknownMemberIsUnknown() {
        ZonedDateTime start = time("2030-01-01T12:00:00Z");
        Ensemble ensemble = new Ensemble("Practice", start);

        assertEquals(MemberStatus.UNKNOWN, ensemble.statusFor(new MemberId(), start.minusMinutes(1)));
    }

    @Test
    public void statusForAcceptedMemberInFutureIsAccepted() {
        ZonedDateTime start = time("2030-01-01T12:00:00Z");
        Ensemble ensemble = new Ensemble("Practice", start);
        MemberId member = new MemberId();
        ensemble.acceptedBy(member);

        assertEquals(MemberStatus.ACCEPTED, ensemble.statusFor(member, start.minusMinutes(1)));
    }

    @Test
    public void statusForAcceptedMemberDuringGracePeriodIsInGracePeriod() {
        ZonedDateTime start = time("2030-01-01T12:00:00Z");
        Ensemble ensemble = new Ensemble("Practice", start);
        MemberId member = new MemberId();
        ensemble.acceptedBy(member);

        assertEquals(MemberStatus.IN_GRACE_PERIOD, ensemble.statusFor(member, start.plusMinutes(5)));
    }

    @Test
    public void statusForAcceptedMemberAfterGracePeriodIsPendingCompleted() {
        ZonedDateTime start = time("2030-01-01T12:00:00Z");
        Ensemble ensemble = new Ensemble("Practice", start);
        MemberId member = new MemberId();
        ensemble.acceptedBy(member);

        assertEquals(MemberStatus.PENDING_COMPLETED, ensemble.statusFor(member, start.plusMinutes(11)));
    }

    @Test
    public void statusForAcceptedMemberAfterCompletionIsCompleted() {
        ZonedDateTime start = time("2030-01-01T12:00:00Z");
        Ensemble ensemble = new Ensemble("Practice", start);
        MemberId member = new MemberId();
        ensemble.acceptedBy(member);
        ensemble.complete();

        assertEquals(MemberStatus.COMPLETED, ensemble.statusFor(member, start.plusHours(2)));
    }

    @Test
    public void statusForAcceptedMemberAfterCancellationIsCanceled() {
        ZonedDateTime start = time("2030-01-01T12:00:00Z");
        Ensemble ensemble = new Ensemble("Practice", start);
        MemberId member = new MemberId();
        ensemble.acceptedBy(member);
        ensemble.cancel();

        assertEquals(MemberStatus.CANCELED, ensemble.statusFor(member, start.minusMinutes(1)));
    }

    @Test
    public void statusForUnknownMemberAfterCancellationIsHidden() {
        ZonedDateTime start = time("2030-01-01T12:00:00Z");
        Ensemble ensemble = new Ensemble("Practice", start);
        ensemble.cancel();

        assertEquals(MemberStatus.HIDDEN, ensemble.statusFor(new MemberId(), start.minusMinutes(1)));
    }

    @Test
    public void rsvpForUnknownMemberIsUnknown() {
        Ensemble ensemble = new Ensemble("Practice", time("2030-01-01T12:00:00Z"));

        assertEquals(Rsvp.UNKNOWN, ensemble.rsvpOf(new MemberId()));
    }

    @Test
    public void declinedMembersAreCounted() {
        Ensemble ensemble = new Ensemble("Practice", time("2030-01-01T12:00:00Z"));
        MemberId member = new MemberId();

        ensemble.declinedBy(member);

        assertEquals(1, ensemble.declinedCount());
        assertEquals(member, ensemble.declinedMembers().findFirst().get());
    }
}
