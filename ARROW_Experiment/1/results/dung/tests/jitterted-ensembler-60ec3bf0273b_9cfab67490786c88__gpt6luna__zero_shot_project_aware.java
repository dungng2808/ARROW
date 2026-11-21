package com.jitterted.mobreg.domain;

import org.junit.jupiter.api.Test;

import java.net.URI;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public class Ensemble_ArrowTest {

    @Test
    public void constructorInitializesNameStartTimeAndMeetingLink() {
        ZonedDateTime start = ZonedDateTime.of(2030, 5, 10, 18, 0, 0, 0, ZoneOffset.UTC);
        Ensemble ensemble = new Ensemble("Friday Dance", start);

        assertEquals("Friday Dance", ensemble.name());
        assertEquals(start, ensemble.startDateTime());
        assertEquals(URI.create("https://zoom.us"), ensemble.meetingLink());
        assertEquals(EnsembleState.SCHEDULED, ensemble.state());
        assertEquals(URI.create(""), ensemble.recordingLink());
    }

    @Test
    public void acceptsMembersAndDoesNotCountDuplicateAcceptance() {
        Ensemble ensemble = new Ensemble("Dance", ZonedDateTime.now(ZoneOffset.UTC));
        MemberId member = new MemberId("member-1");

        ensemble.acceptedBy(member);
        ensemble.acceptedBy(member);

        assertEquals(1, ensemble.acceptedCount());
        assertTrue(ensemble.isAccepted(member));
        assertEquals(Rsvp.ACCEPTED, ensemble.rsvpOf(member));
        assertEquals(Set.of(member), ensemble.acceptedMembers().collect(Collectors.toSet()));
    }

    @Test
    public void acceptingMemberRemovesTheirDecline() {
        Ensemble ensemble = new Ensemble("Dance", ZonedDateTime.now(ZoneOffset.UTC));
        MemberId member = new MemberId("member-1");

        ensemble.declinedBy(member);
        ensemble.acceptedBy(member);

        assertFalse(ensemble.isDeclined(member));
        assertEquals(0, ensemble.declinedCount());
        assertEquals(Rsvp.ACCEPTED, ensemble.rsvpOf(member));
    }

    @Test
    public void decliningMemberRemovesTheirAcceptance() {
        Ensemble ensemble = new Ensemble("Dance", ZonedDateTime.now(ZoneOffset.UTC));
        MemberId member = new MemberId("member-1");

        ensemble.acceptedBy(member);
        ensemble.declinedBy(member);

        assertFalse(ensemble.isAccepted(member));
        assertTrue(ensemble.isDeclined(member));
        assertEquals(0, ensemble.acceptedCount());
        assertEquals(Rsvp.DECLINED, ensemble.rsvpOf(member));
        assertEquals(Set.of(member), ensemble.declinedMembers().collect(Collectors.toSet()));
    }

    @Test
    public void spectatorIsRemovedFromAcceptedAndDeclinedMembership() {
        Ensemble ensemble = new Ensemble("Dance", ZonedDateTime.now(ZoneOffset.UTC));
        MemberId member = new MemberId("member-1");

        ensemble.acceptedBy(member);
        ensemble.joinAsSpectator(member);

        assertFalse(ensemble.isAccepted(member));
        assertFalse(ensemble.isDeclined(member));
        assertEquals(Rsvp.UNKNOWN, ensemble.rsvpOf(member));
        assertTrue(ensemble.spectators().contains(member));
    }

    @Test
    public void acceptsUpToFiveMembers() {
        Ensemble ensemble = new Ensemble("Dance", ZonedDateTime.now(ZoneOffset.UTC));
        MemberId[] members = {
                new MemberId("member-1"),
                new MemberId("member-2"),
                new MemberId("member-3"),
                new MemberId("member-4"),
                new MemberId("member-5")
        };

        for (MemberId member : members) {
            ensemble.acceptedBy(member);
        }

        assertEquals(5, ensemble.acceptedCount());
        assertFalse(ensemble.canAccept());
    }

    @Test
    public void rejectsAcceptanceWhenFull() {
        Ensemble ensemble = new Ensemble("Dance", ZonedDateTime.now(ZoneOffset.UTC));
        for (int i = 1; i <= 5; i++) {
            ensemble.acceptedBy(new MemberId("member-" + i));
        }

        assertThrows(EnsembleFullException.class,
                () -> ensemble.acceptedBy(new MemberId("member-6")));
    }

    @Test
    public void changesMeetingLink() {
        Ensemble ensemble = new Ensemble("Dance", ZonedDateTime.now(ZoneOffset.UTC));
        URI link = URI.create("https://example.com/meeting");

        ensemble.changeMeetingLinkTo(link);

        assertEquals(link, ensemble.meetingLink());
        assertEquals(link, ensemble.conferenceDetails().joinUrl());
    }

    @Test
    public void changesNameAndStartTime() {
        ZonedDateTime original = ZonedDateTime.of(2030, 5, 10, 18, 0, 0, 0, ZoneOffset.UTC);
        ZonedDateTime changed = original.plusDays(1);
        Ensemble ensemble = new Ensemble("Dance", original);

        ensemble.changeNameTo("New Dance");
        ensemble.changeStartDateTimeTo(changed);

        assertEquals("New Dance", ensemble.name());
        assertEquals(changed, ensemble.startDateTime());
    }

    @Test
    public void rejectsNullName() {
        Ensemble ensemble = new Ensemble("Dance", ZonedDateTime.now(ZoneOffset.UTC));

        assertThrows(IllegalArgumentException.class, () -> ensemble.changeNameTo(null));
    }

    @Test
    public void rejectsNullStartTime() {
        Ensemble ensemble = new Ensemble("Dance", ZonedDateTime.now(ZoneOffset.UTC));

        assertThrows(IllegalArgumentException.class, () -> ensemble.changeStartDateTimeTo(null));
    }

    @Test
    public void linksRecording() {
        Ensemble ensemble = new Ensemble("Dance", ZonedDateTime.now(ZoneOffset.UTC));
        URI recording = URI.create("https://example.com/recording");

        ensemble.linkToRecordingAt(recording);

        assertEquals(recording, ensemble.recordingLink());
    }

    @Test
    public void completesEnsembleAndReportsAcceptedMemberCompleted() {
        ZonedDateTime start = ZonedDateTime.of(2030, 5, 10, 18, 0, 0, 0, ZoneOffset.UTC);
        Ensemble ensemble = new Ensemble("Dance", start);
        MemberId member = new MemberId("member-1");
        ensemble.acceptedBy(member);

        ensemble.complete();

        assertTrue(ensemble.isCompleted());
        assertEquals(EnsembleState.COMPLETED, ensemble.state());
        assertEquals(MemberStatus.COMPLETED, ensemble.statusFor(member, start.minusMinutes(1)));
    }

    @Test
    public void cannotAcceptAfterCompletion() {
        Ensemble ensemble = new Ensemble("Dance", ZonedDateTime.now(ZoneOffset.UTC));
        ensemble.complete();

        assertThrows(EnsembleCompleted.class,
                () -> ensemble.acceptedBy(new MemberId("member-1")));
    }

    @Test
    public void cancelsEnsembleAndHidesUnknownMember() {
        ZonedDateTime start = ZonedDateTime.of(2030, 5, 10, 18, 0, 0, 0, ZoneOffset.UTC);
        Ensemble ensemble = new Ensemble("Dance", start);

        ensemble.cancel();

        assertTrue(ensemble.isCanceled());
        assertEquals(EnsembleState.CANCELED, ensemble.state());
        assertEquals(MemberStatus.HIDDEN,
                ensemble.statusFor(new MemberId("member-1"), start.minusDays(1)));
    }

    @Test
    public void cannotAcceptAfterCancellation() {
        Ensemble ensemble = new Ensemble("Dance", ZonedDateTime.now(ZoneOffset.UTC));
        ensemble.cancel();

        assertThrows(EnsembleCanceled.class,
                () -> ensemble.acceptedBy(new MemberId("member-1")));
    }

    @Test
    public void cannotCancelCompletedEnsemble() {
        Ensemble ensemble = new Ensemble("Dance", ZonedDateTime.now(ZoneOffset.UTC));
        ensemble.complete();

        assertThrows(EnsembleCompleted.class, ensemble::cancel);
    }

    @Test
    public void cannotCompleteCanceledEnsemble() {
        Ensemble ensemble = new Ensemble("Dance", ZonedDateTime.now(ZoneOffset.UTC));
        ensemble.cancel();

        assertThrows(EnsembleCanceled.class, ensemble::complete);
    }

    @Test
    public void reportsFutureStatuses() {
        ZonedDateTime start = ZonedDateTime.of(2030, 5, 10, 18, 0, 0, 0, ZoneOffset.UTC);
        Ensemble ensemble = new Ensemble("Dance", start);
        MemberId accepted = new MemberId("accepted");
        MemberId declined = new MemberId("declined");
        MemberId unknown = new MemberId("unknown");
        ensemble.acceptedBy(accepted);
        ensemble.declinedBy(declined);

        assertEquals(MemberStatus.ACCEPTED, ensemble.statusFor(accepted, start.minusMinutes(1)));
        assertEquals(MemberStatus.DECLINED, ensemble.statusFor(declined, start.minusMinutes(1)));
        assertEquals(MemberStatus.UNKNOWN, ensemble.statusFor(unknown, start.minusMinutes(1)));
    }

    @Test
    public void reportsInGracePeriodStatusForVisibleMembers() {
        ZonedDateTime start = ZonedDateTime.of(2030, 5, 10, 18, 0, 0, 0, ZoneOffset.UTC);
        Ensemble ensemble = new Ensemble("Dance", start);
        MemberId member = new MemberId("member-1");
        ensemble.acceptedBy(member);

        assertEquals(MemberStatus.IN_GRACE_PERIOD,
                ensemble.statusFor(member, start.plusMinutes(5)));
    }

    @Test
    public void hidesUnknownMemberAfterStartGracePeriod() {
        ZonedDateTime start = ZonedDateTime.of(2030, 5, 10, 18, 0, 0, 0, ZoneOffset.UTC);
        Ensemble ensemble = new Ensemble("Dance", start);

        assertEquals(MemberStatus.HIDDEN,
                ensemble.statusFor(new MemberId("member-1"), start.plusMinutes(11)));
    }

    @Test
    public void changesAndRemovesConferenceDetails() {
        Ensemble ensemble = new Ensemble("Dance", ZonedDateTime.now(ZoneOffset.UTC));
        ConferenceDetails details = ensemble.conferenceDetails();

        ensemble.changeConferenceDetailsTo(details);

        assertSame(details, ensemble.conferenceDetails());

        ensemble.removeConferenceDetails();

        assertSame(ConferenceDetails.DELETED, ensemble.conferenceDetails());
    }

    @Test
    public void storesAndReturnsId() {
        Ensemble ensemble = new Ensemble("Dance", ZonedDateTime.now(ZoneOffset.UTC));
        EnsembleId id = new EnsembleId("ensemble-1");

        ensemble.setId(id);

        assertSame(id, ensemble.getId());
    }
}
