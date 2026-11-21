package com.jitterted.mobreg.domain;

import org.junit.Test;

import java.net.URI;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class Ensemble_ArrowTest {

    private ZonedDateTime start() {
        return ZonedDateTime.of(2030, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC);
    }

    @Test
    public void constructorInitializesNameStartAndMeetingLink() {
        Ensemble ensemble = new Ensemble("Quartet", start());

        assertEquals("Quartet", ensemble.name());
        assertEquals(start(), ensemble.startDateTime());
        assertEquals(URI.create("https://zoom.us"), ensemble.meetingLink());
        assertEquals(0, ensemble.acceptedCount());
        assertEquals(0, ensemble.declinedCount());
        assertEquals(EnsembleState.SCHEDULED, ensemble.state());
    }

    @Test
    public void constructorAcceptsCustomMeetingLink() {
        URI link = URI.create("https://example.com/meeting");
        Ensemble ensemble = new Ensemble("Quartet", link, start());

        assertEquals(link, ensemble.meetingLink());
    }

    @Test
    public void idCanBeSetAndRetrieved() {
        Ensemble ensemble = new Ensemble("Quartet", start());
        EnsembleId id = new EnsembleId("ensemble-1");

        ensemble.setId(id);

        assertEquals(id, ensemble.getId());
    }

    @Test
    public void acceptsMemberAndReportsRsvp() {
        Ensemble ensemble = new Ensemble("Quartet", start());
        MemberId member = new MemberId("member-1");

        ensemble.acceptedBy(member);

        assertEquals(1, ensemble.acceptedCount());
        assertTrue(ensemble.isAccepted(member));
        assertEquals(Rsvp.ACCEPTED, ensemble.rsvpOf(member));
        assertEquals(new HashSet<MemberId>(Arrays.asList(member)),
                ensemble.acceptedMembers().collect(Collectors.toSet()));
    }

    @Test
    public void acceptingSameMemberTwiceDoesNotIncreaseCount() {
        Ensemble ensemble = new Ensemble("Quartet", start());
        MemberId member = new MemberId("member-1");

        ensemble.acceptedBy(member);
        ensemble.acceptedBy(member);

        assertEquals(1, ensemble.acceptedCount());
    }

    @Test
    public void decliningMemberRemovesAcceptanceAndReportsDeclined() {
        Ensemble ensemble = new Ensemble("Quartet", start());
        MemberId member = new MemberId("member-1");
        ensemble.acceptedBy(member);

        ensemble.declinedBy(member);

        assertEquals(0, ensemble.acceptedCount());
        assertEquals(1, ensemble.declinedCount());
        assertFalse(ensemble.isAccepted(member));
        assertTrue(ensemble.isDeclined(member));
        assertEquals(Rsvp.DECLINED, ensemble.rsvpOf(member));
        assertEquals(new HashSet<MemberId>(Arrays.asList(member)),
                ensemble.declinedMembers().collect(Collectors.toSet()));
    }

    @Test
    public void acceptingPreviouslyDeclinedMemberClearsDecline() {
        Ensemble ensemble = new Ensemble("Quartet", start());
        MemberId member = new MemberId("member-1");
        ensemble.declinedBy(member);

        ensemble.acceptedBy(member);

        assertTrue(ensemble.isAccepted(member));
        assertFalse(ensemble.isDeclined(member));
        assertEquals(0, ensemble.declinedCount());
    }

    @Test
    public void unknownMemberHasUnknownRsvp() {
        Ensemble ensemble = new Ensemble("Quartet", start());

        assertEquals(Rsvp.UNKNOWN, ensemble.rsvpOf(new MemberId("member-1")));
    }

    @Test
    public void canAcceptUntilFiveMembersHaveAccepted() {
        Ensemble ensemble = new Ensemble("Quartet", start());
        ensemble.acceptedBy(new MemberId("member-1"));
        ensemble.acceptedBy(new MemberId("member-2"));
        ensemble.acceptedBy(new MemberId("member-3"));
        ensemble.acceptedBy(new MemberId("member-4"));

        assertTrue(ensemble.canAccept());

        ensemble.acceptedBy(new MemberId("member-5"));

        assertFalse(ensemble.canAccept());
        assertEquals(5, ensemble.acceptedCount());
    }

    @Test(expected = EnsembleFullException.class)
    public void cannotAcceptSixthMember() {
        Ensemble ensemble = new Ensemble("Quartet", start());
        ensemble.acceptedBy(new MemberId("member-1"));
        ensemble.acceptedBy(new MemberId("member-2"));
        ensemble.acceptedBy(new MemberId("member-3"));
        ensemble.acceptedBy(new MemberId("member-4"));
        ensemble.acceptedBy(new MemberId("member-5"));

        ensemble.acceptedBy(new MemberId("member-6"));
    }

    @Test
    public void joiningAsSpectatorRemovesRsvpAndAddsSpectator() {
        Ensemble ensemble = new Ensemble("Quartet", start());
        MemberId member = new MemberId("member-1");
        ensemble.acceptedBy(member);

        ensemble.joinAsSpectator(member);

        assertEquals(0, ensemble.acceptedCount());
        assertEquals(0, ensemble.declinedCount());
        assertEquals(Rsvp.UNKNOWN, ensemble.rsvpOf(member));
        assertTrue(ensemble.spectators().contains(member));
    }

    @Test
    public void spectatorSetCanBeRead() {
        Ensemble ensemble = new Ensemble("Quartet", start());
        MemberId member = new MemberId("member-1");

        ensemble.joinAsSpectator(member);

        Set<MemberId> spectators = ensemble.spectators();
        assertEquals(new HashSet<MemberId>(Arrays.asList(member)), spectators);
    }

    @Test
    public void changesMeetingLink() {
        Ensemble ensemble = new Ensemble("Quartet", start());
        URI link = URI.create("https://example.com/new");

        ensemble.changeMeetingLinkTo(link);

        assertEquals(link, ensemble.meetingLink());
    }

    @Test
    public void changesName() {
        Ensemble ensemble = new Ensemble("Quartet", start());

        ensemble.changeNameTo("New Quartet");

        assertEquals("New Quartet", ensemble.name());
    }

    @Test(expected = IllegalArgumentException.class)
    public void changingNameToNullThrows() {
        Ensemble ensemble = new Ensemble("Quartet", start());

        ensemble.changeNameTo(null);
    }

    @Test
    public void changesStartDateTime() {
        Ensemble ensemble = new Ensemble("Quartet", start());
        ZonedDateTime newStart = start().plusDays(1);

        ensemble.changeStartDateTimeTo(newStart);

        assertEquals(newStart, ensemble.startDateTime());
    }

    @Test(expected = IllegalArgumentException.class)
    public void changingStartDateTimeToNullThrows() {
        Ensemble ensemble = new Ensemble("Quartet", start());

        ensemble.changeStartDateTimeTo(null);
    }

    @Test
    public void completesEnsemble() {
        Ensemble ensemble = new Ensemble("Quartet", start());

        ensemble.complete();

        assertTrue(ensemble.isCompleted());
        assertEquals(EnsembleState.COMPLETED, ensemble.state());
    }

    @Test
    public void cancelsEnsemble() {
        Ensemble ensemble = new Ensemble("Quartet", start());

        ensemble.cancel();

        assertTrue(ensemble.isCanceled());
        assertEquals(EnsembleState.CANCELED, ensemble.state());
    }

    @Test(expected = EnsembleCanceled.class)
    public void cannotAcceptAfterCancellation() {
        Ensemble ensemble = new Ensemble("Quartet", start());
        ensemble.cancel();

        ensemble.acceptedBy(new MemberId("member-1"));
    }

    @Test(expected = EnsembleCompleted.class)
    public void cannotAcceptAfterCompletion() {
        Ensemble ensemble = new Ensemble("Quartet", start());
        ensemble.complete();

        ensemble.acceptedBy(new MemberId("member-1"));
    }

    @Test(expected = EnsembleCompleted.class)
    public void cannotCancelCompletedEnsemble() {
        Ensemble ensemble = new Ensemble("Quartet", start());
        ensemble.complete();

        ensemble.cancel();
    }

    @Test(expected = EnsembleCanceled.class)
    public void cannotCompleteCanceledEnsemble() {
        Ensemble ensemble = new Ensemble("Quartet", start());
        ensemble.cancel();

        ensemble.complete();
    }

    @Test
    public void changesRecordingLink() {
        Ensemble ensemble = new Ensemble("Quartet", start());
        URI recording = URI.create("https://example.com/recording");

        ensemble.linkToRecordingAt(recording);

        assertEquals(recording, ensemble.recordingLink());
    }

    @Test
    public void recordingLinkDefaultsToEmptyUri() {
        Ensemble ensemble = new Ensemble("Quartet", start());

        assertEquals(URI.create(""), ensemble.recordingLink());
    }

    @Test
    public void changesConferenceDetails() {
        Ensemble ensemble = new Ensemble("Quartet", start());
        ConferenceDetails details = new ConferenceDetails(
                "Meeting", URI.create("https://example.com/info"),
                URI.create("https://example.com/join"));

        ensemble.changeConferenceDetailsTo(details);

        assertEquals(details, ensemble.conferenceDetails());
    }

    @Test
    public void removesConferenceDetails() {
        Ensemble ensemble = new Ensemble("Quartet", start());

        ensemble.removeConferenceDetails();

        assertEquals(ConferenceDetails.DELETED, ensemble.conferenceDetails());
    }

    @Test
    public void statusForFutureUnknownMemberIsUnknown() {
        Ensemble ensemble = new Ensemble("Quartet", start());
        ZonedDateTime now = start().minusMinutes(1);

        assertEquals(MemberStatus.UNKNOWN, ensemble.statusFor(new MemberId("member-1"), now));
    }

    @Test
    public void statusForFutureAcceptedMemberIsAccepted() {
        Ensemble ensemble = new Ensemble("Quartet", start());
        MemberId member = new MemberId("member-1");
        ensemble.acceptedBy(member);

        assertEquals(MemberStatus.ACCEPTED, ensemble.statusFor(member, start().minusMinutes(1)));
    }

    @Test
    public void statusForFutureDeclinedMemberIsDeclined() {
        Ensemble ensemble = new Ensemble("Quartet", start());
        MemberId member = new MemberId("member-1");
        ensemble.declinedBy(member);

        assertEquals(MemberStatus.DECLINED, ensemble.statusFor(member, start().minusMinutes(1)));
    }

    @Test
    public void statusDuringGracePeriodIsInGracePeriod() {
        Ensemble ensemble = new Ensemble("Quartet", start());

        assertEquals(MemberStatus.IN_GRACE_PERIOD,
                ensemble.statusFor(new MemberId("member-1"), start().plusMinutes(5)));
    }

    @Test
    public void statusAfterGracePeriodDuringEnsembleIsHidden() {
        Ensemble ensemble = new Ensemble("Quartet", start());

        assertEquals(MemberStatus.HIDDEN,
                ensemble.statusFor(new MemberId("member-1"), start().plusMinutes(11)));
    }

    @Test
    public void statusForAcceptedMemberAfterStartIsPendingCompleted() {
        Ensemble ensemble = new Ensemble("Quartet", start());
        MemberId member = new MemberId("member-1");
        ensemble.acceptedBy(member);

        assertEquals(MemberStatus.PENDING_COMPLETED,
                ensemble.statusFor(member, start().plusMinutes(11)));
    }

    @Test
    public void statusForAcceptedMemberAfterCompletionIsCompleted() {
        Ensemble ensemble = new Ensemble("Quartet", start());
        MemberId member = new MemberId("member-1");
        ensemble.acceptedBy(member);
        ensemble.complete();

        assertEquals(MemberStatus.COMPLETED, ensemble.statusFor(member, start().plusHours(2)));
    }

    @Test
    public void statusForAcceptedMemberOnCanceledEnsembleIsCanceled() {
        Ensemble ensemble = new Ensemble("Quartet", start());
        MemberId member = new MemberId("member-1");
        ensemble.acceptedBy(member);
        ensemble.cancel();

        assertEquals(MemberStatus.CANCELED, ensemble.statusFor(member, start().minusMinutes(1)));
    }

    @Test
    public void statusForUnknownMemberOnCanceledEnsembleIsHidden() {
        Ensemble ensemble = new Ensemble("Quartet", start());
        ensemble.cancel();

        assertEquals(MemberStatus.HIDDEN,
                ensemble.statusFor(new MemberId("member-1"), start().minusMinutes(1)));
    }

    @Test
    public void statusForAcceptedMemberAfterEnsembleDurationIsPendingCompleted() {
        Ensemble ensemble = new Ensemble("Quartet", start());
        MemberId member = new MemberId("member-1");
        ensemble.acceptedBy(member);

        assertEquals(MemberStatus.PENDING_COMPLETED,
                ensemble.statusFor(member, start().plusHours(2)));
    }

    @Test
    public void statusForCompletedEnsembleHidesUnknownMember() {
        Ensemble ensemble = new Ensemble("Quartet", start());
        ensemble.complete();

        assertEquals(MemberStatus.HIDDEN,
                ensemble.statusFor(new MemberId("member-1"), start().plusHours(2)));
    }

    @Test
    public void statusForFullEnsembleUnknownMemberIsFull() {
        Ensemble ensemble = new Ensemble("Quartet", start());
        ensemble.acceptedBy(new MemberId("member-1"));
        ensemble.acceptedBy(new MemberId("member-2"));
        ensemble.acceptedBy(new MemberId("member-3"));
        ensemble.acceptedBy(new MemberId("member-4"));
        ensemble.acceptedBy(new MemberId("member-5"));

        assertEquals(MemberStatus.FULL,
                ensemble.statusFor(new MemberId("member-6"), start().minusMinutes(1)));
    }

    @Test
    public void statusForFullEnsembleDeclinedMemberIsDeclinedFull() {
        Ensemble ensemble = new Ensemble("Quartet", start());
        ensemble.acceptedBy(new MemberId("member-1"));
        ensemble.acceptedBy(new MemberId("member-2"));
        ensemble.acceptedBy(new MemberId("member-3"));
        ensemble.acceptedBy(new MemberId("member-4"));
        ensemble.acceptedBy(new MemberId("member-5"));
        MemberId declined = new MemberId("member-6");
        ensemble.declinedBy(declined);

        assertEquals(MemberStatus.DECLINED_FULL,
                ensemble.statusFor(declined, start().minusMinutes(1)));
    }

    @Test
    public void statusForFullEnsembleAcceptedMemberIsAccepted() {
        Ensemble ensemble = new Ensemble("Quartet", start());
        MemberId member = new MemberId("member-1");
        ensemble.acceptedBy(member);
        ensemble.acceptedBy(new MemberId("member-2"));
        ensemble.acceptedBy(new MemberId("member-3"));
        ensemble.acceptedBy(new MemberId("member-4"));
        ensemble.acceptedBy(new MemberId("member-5"));

        assertEquals(MemberStatus.ACCEPTED,
                ensemble.statusFor(member, start().minusMinutes(1)));
    }
}
