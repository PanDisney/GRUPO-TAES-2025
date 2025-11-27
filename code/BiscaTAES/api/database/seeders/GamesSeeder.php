<?php

namespace Database\Seeders;

use Illuminate\Database\Console\Seeds\WithoutModelEvents;
use Illuminate\Database\Seeder;
use Illuminate\Support\Facades\DB;
use Carbon\Carbon;
use Illuminate\Container\Attributes\Database;

class GamesSeeder extends Seeder
{
    private $ratioStandaloneToMatch = 15;

    private function calculateRandomSeconds($filteredCollection)
    {
        $totalPlayers = $filteredCollection->count() + 1;
        return (12 * 60 * 60) / $totalPlayers + rand(0, 2000);
    }

    private function nextGameDateTime(&$d, $filteredPlayers, $withinSameMatch = false) {
        if ($withinSameMatch) {
            $deltaSegundos = rand(300, 900);
        } else {
            $deltaSegundos = $this->calculateRandomSeconds($filteredPlayers);
        }
        $d->addSeconds($deltaSegundos);
    }

    public function run(): void
    {
        $this->command->info("Games seeder - Start");

        $start = DB::table('users')->where('type', 'P')->min('created_at');

        $allPlayers = DB::table('users')->where('type', 'P')->get();
        $sortedPlayers = $allPlayers->sortBy('created_at')->values();

        $d = new \Carbon\Carbon($start);
        $d = $d->addDay();
        $now = \Carbon\Carbon::now();
        $this->command->info("Starting to create games");

        $games = [];
        $matches = [];

        $i = 0;

        $filteredPlayers = null;
        $filteredPlayersIds = null;
        $nextCreatedAt = null;

        while ($d->lte($now)) {
            $i++;
            if (($filteredPlayers === null) || ($nextCreatedAt === null) ||
                ($d->gte($nextCreatedAt))
            ) {
                $filteredPlayers = $allPlayers->filter(function ($value) use ($d) {
                    return $d->gt($value->created_at);
                });
                $nextCreatedAtPlayer = $sortedPlayers->first(function ($value) use ($d) {
                    return $d->lte($value->created_at);
                });
                $nextCreatedAt = $nextCreatedAtPlayer ? $nextCreatedAtPlayer->created_at : new \Carbon\Carbon('9999-12-31');
                $filteredPlayersIds = $filteredPlayers->pluck('id')->toArray();
            }

            // Only creates games or matches when there are enough players
            if ($filteredPlayersIds === null || empty($filteredPlayersIds) || count($filteredPlayersIds) <2) {
                $this->nextGameDateTime($d, $filteredPlayers);
                continue;
            }

            $userIdKeys = array_rand($filteredPlayersIds, 2);
            $userIDPlayer1 = $filteredPlayersIds[$userIdKeys[0]];
            $userIDPlayer2 = $filteredPlayersIds[$userIdKeys[1]];

            if ($userIDPlayer1 == $userIDPlayer2) {
                $this->nextGameDateTime($d, $filteredPlayers);
                continue;
            }
            $match = null;
            if (rand(1, $this->ratioStandaloneToMatch) === 1) {
                $match = $this->newMatch($filteredPlayers, $userIDPlayer1, $userIDPlayer2, $d);
                $playersMarks = [0, 0];
                $playersPoints = [0, 0];
                while($playersMarks[0] < 4 && $playersMarks[1] <4) {
                    $newGame = $this->newGame($filteredPlayers, $match, $userIDPlayer1, $userIDPlayer2, $d);
                    $games[] = $newGame;
                    $playersPoints[0] += $newGame['player1_points'];
                    $playersPoints[1] += $newGame['player2_points'];
                    if ($newGame['player1_points'] > $newGame['player2_points']) {
                        if ($newGame['player1_points'] >= 120) {
                            $playersMarks[0]+= 4;
                        } elseif ($newGame['player1_points'] >= 91) {
                            $playersMarks[0] += 2;
                        } else {
                            $playersMarks[0]++;
                        }
                    } elseif ($newGame['player2_points'] > $newGame['player1_points']) {
                        if ($newGame['player2_points'] >= 120) {
                            $playersMarks[1]+= 4;
                        } elseif ($newGame['player2_points'] >= 91) {
                            $playersMarks[1] += 2;
                        } else {
                            $playersMarks[1]++;
                        }
                    }
                }
                $this->updateMatchWinner($match, $playersMarks[0], $playersMarks[1], $playersPoints[0], $playersPoints[1], $d);
                $matches[] = $match;
            } else {
                $newGame = $this->newGame($filteredPlayers, $match, $userIDPlayer1, $userIDPlayer2, $d);
                $games[] = $newGame;
            }

            if ($i >= DatabaseSeeder::$dbInsertBlockSize) {
                if (!empty($matches)) {
                    DB::table('matches')->insert($matches);
                    $this->command->info("Saved " . count($matches) . " matches at date " . $d->format('Y-m-d H:i:s'));
                }
                if (!empty($games)) {
                    DB::table('games')->insert($games);
                    $this->command->info("Saved " . count($games) . " games at date " . $d->format('Y-m-d H:i:s'));
                }
                $i = 0;
                $games = [];
                $matches = [];
            }
            //$this->nextGameDateTime($d, $filteredPlayers);
        }
        if (!empty($matches)) {
            DB::table('matches')->insert($matches);
            $this->command->info("Saved " . count($matches) . " matches at date " . $d->format('Y-m-d H:i:s'));
        }
        if (!empty($games)) {
            DB::table('games')->insert($games);
            $this->command->info("Saved " . count($games) . " games at date " . $d->format('Y-m-d H:i:s'));
        }
        $this->command->info("Games seeder - End");
    }

    private $matchID = 0;
    private function newMatch($filteredPlayers,$user1, $user2, $d)
    {
        $this->matchID++;
        $this->nextGameDateTime($d, $filteredPlayers);
        return [
            'id' => $this->matchID,
            'type' => random_int(1,2) == 1 ? '3' : '9',
            'player1_user_id' => $user1,
            'player2_user_id' => $user2,
            'winner_user_id' => null,
            'loser_user_id' => null,
            'status' => 'Ended',
            'stake' => random_int(1,4) > 1 ? 3 : random_int(4,100),
            'began_at' => $d->copy(),
            'ended_at' => null,
            'total_time' => null,
            'player1_marks' => null,
            'player2_marks' => null,
            'player1_points' => null,
            'player2_points' => null,
            'custom' => null
        ];
    }

    private function updateMatchWinner(&$match, $player1Marks, $player2Marks, $totalPlayers1, $totalPlayers2, $d)
    {
        $match['player1_marks'] = $player1Marks;
        $match['player2_marks'] = $player2Marks;
        $match['player1_points'] = $totalPlayers1;
        $match['player2_points'] = $totalPlayers2;
        $match['ended_at'] = $d->copy();
        $match['total_time'] = $match['began_at']->diffInSeconds($match['ended_at']);
        $match['winner_user_id'] = $player1Marks > $player2Marks ? $match['player1_user_id'] : ($player2Marks > $player1Marks ? $match['player2_user_id'] : null);
        $match['loser_user_id'] = $player1Marks > $player2Marks ? $match['player2_user_id'] : ($player2Marks > $player1Marks ? $match['player1_user_id'] : null);
    }

    private $gameID = 0;
    private function newGame($filteredPlayers, $match, $user1, $user2, $d)
    {
        $this->gameID++;
        $this->nextGameDateTime($d, $filteredPlayers, $match != null);
        $begin_d = $d->copy();
        $pointsUser1 = 60;
        $pointsUser2 = 60;
        // if random == 1 it is a draw
        if (random_int(1,30) > 1) {
            $pointsUser1 = rand(0, 120);
            $pointsUser2 = 120 - $pointsUser1;
        }
        $duration = random_int(200, 900);
        $d->addSeconds($duration);

        return [
            'id' => $this->gameID,
            'type' => $match ? $match['type'] : (random_int(1, 2) == 1 ? '3' : '9'),
            'match_id' => $match ? $match['id'] : null,
            'player1_user_id' => $user1,
            'player2_user_id' => $user2,
            'is_draw' => $pointsUser1 == $pointsUser2,
            'winner_user_id' => $pointsUser1 > $pointsUser2 ? $user1 : ($pointsUser2 > $pointsUser1 ? $user2 : null),
            'loser_user_id' => $pointsUser1 < $pointsUser2 ? $user1 : ($pointsUser2 < $pointsUser1 ? $user2 : null),
            'status' => 'Ended',
            'began_at' => $begin_d,
            'ended_at' => $d->copy(),
            'total_time' => $duration,
            'player1_points' => $pointsUser1,
            'player2_points' => $pointsUser2,
            'custom' => null
        ];
    }
}
