<?php

namespace App\Services;

class BiscaGameService
{
    // Enum for Suits
    const SUIT_HEARTS = 'HEARTS';
    const SUIT_DIAMONDS = 'DIAMONDS';
    const SUIT_CLUBS = 'CLUBS';
    const SUIT_SPADES = 'SPADES';

    // Enum for Ranks
    const RANK_TWO = 'TWO';
    const RANK_THREE = 'THREE';
    const RANK_FOUR = 'FOUR';
    const RANK_FIVE = 'FIVE';
    const RANK_SIX = 'SIX';
    const RANK_QUEEN = 'QUEEN';
    const RANK_JACK = 'JACK';
    const RANK_KING = 'KING';
    const RANK_SEVEN = 'SEVEN';
    const RANK_ACE = 'ACE';

    private static $rankPoints = [
        self::RANK_TWO => 0,
        self::RANK_THREE => 0,
        self::RANK_FOUR => 0,
        self::RANK_FIVE => 0,
        self::RANK_SIX => 0,
        self::RANK_QUEEN => 2,
        self::RANK_JACK => 3,
        self::RANK_KING => 4,
        self::RANK_SEVEN => 10,
        self::RANK_ACE => 11,
    ];

    private static $rankStrength = [
        self::RANK_TWO => 1,
        self::RANK_THREE => 2,
        self::RANK_FOUR => 3,
        self::RANK_FIVE => 4,
        self::RANK_SIX => 5,
        self::RANK_QUEEN => 6,
        self::RANK_JACK => 7,
        self::RANK_KING => 8,
        self::RANK_SEVEN => 9,
        self::RANK_ACE => 10,
    ];

    public static function parseCard(string $cardString): ?array
    {
        $parts = explode('_OF_', $cardString);
        if (count($parts) !== 2) {
            return null;
        }

        $rankString = $parts[0];
        $suitString = $parts[1];

        if (!defined("self::RANK_$rankString") || !defined("self::SUIT_$suitString")) {
            return null;
        }

        return [
            'rank' => constant("self::RANK_$rankString"),
            'suit' => constant("self::SUIT_$suitString"),
            'points' => self::$rankPoints[constant("self::RANK_$rankString")],
            'strength' => self::$rankStrength[constant("self::RANK_$rankString")],
        ];
    }

    public static function determineTrickWinner(array $cardPlayer1, array $cardPlayer2, string $trumpSuit, int $leaderId, int $player1Id, int $player2Id): int
    {
        $card1IsTrump = ($cardPlayer1['suit'] === $trumpSuit);
        $card2IsTrump = ($cardPlayer2['suit'] === $trumpSuit);

        // Case 1: Both are trumps
        if ($card1IsTrump && $card2IsTrump) {
            return $cardPlayer1['strength'] > $cardPlayer2['strength'] ? $player1Id : $player2Id;
        }

        // Case 2: Only card 1 is trump
        if ($card1IsTrump) {
            return $player1Id;
        }

        // Case 3: Only card 2 is trump
        if ($card2IsTrump) {
            return $player2Id;
        }

        // Case 4: No trumps, different suits
        if ($cardPlayer1['suit'] !== $cardPlayer2['suit']) {
            return $leaderId;
        }

        // Case 5: No trumps, same suit
        return $cardPlayer1['strength'] > $cardPlayer2['strength'] ? $player1Id : $player2Id;
    }
}
