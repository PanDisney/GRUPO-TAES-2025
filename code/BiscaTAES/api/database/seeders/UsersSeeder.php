<?php

namespace Database\Seeders;

use Illuminate\Database\Console\Seeds\WithoutModelEvents;
use Illuminate\Database\Seeder;
use Faker\Factory;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\File;
use stdClass;

class UsersSeeder extends Seeder
{
    use SeederUtils;

    private $faker = null;
    private $files_M = [];
    private $files_F = [];
    public static $hashPasword = "";
    public static $dbUsers = null;


    public static $fixedUsers = [
        ['type' => 'A', 'name' => 'First Administrator', 'email' => 'a1@mail.pt', 'gender' => 'M', 'softdelete' => false],
        ['type' => 'A', 'name' => 'Second Administrator', 'email' => 'a2@mail.pt', 'gender' => 'F', 'softdelete' => false],
        ['type' => 'A', 'name' => 'Third Administrator', 'email' => 'a3@mail.pt', 'gender' => 'M', 'softdelete' => false],
        ['type' => 'A', 'name' => 'Forth Administrator', 'email' => 'a4@mail.pt', 'gender' => 'M', 'softdelete' => true],

        ['type' => 'P', 'name' => 'Bisca Bot', 'email' => 'bot@bisca.pt', 'gender' => 'M', 'softdelete' => false, 'nickname' => 'Bot'],
        ['type' => 'P', 'name' => 'Player A', 'email' => 'pa@mail.pt', 'gender' => 'M', 'softdelete' => false],
        ['type' => 'P', 'name' => 'Player B', 'email' => 'pb@mail.pt', 'gender' => 'M', 'softdelete' => false],
        ['type' => 'P', 'name' => 'Player C', 'email' => 'pc@mail.pt', 'gender' => 'F', 'softdelete' => false],
        ['type' => 'P', 'name' => 'Player D', 'email' => 'pd@mail.pt', 'gender' => 'M', 'softdelete' => true],
        ['type' => 'P', 'name' => 'Player E', 'email' => 'pe@mail.pt', 'gender' => 'F', 'softdelete' => false],
        ['type' => 'P', 'name' => 'Player F', 'email' => 'pf@mail.pt', 'gender' => 'M', 'softdelete' => true],
        ['type' => 'P', 'name' => 'Francisco Silva', 'email' => 'aluno@mail.com', 'gender' => 'M', 'softdelete' => false, 'nickname' => 'Paquinho'],
    ];

    public static $userTypes = [
        'A' => 10,
        'P' => 500];

    public function run(): void
    {
        $this->command->line("Creating Users.");
        self::$hashPasword = bcrypt('123');

        //$this->faker = Factory::create('pt_PT');
        $this->faker = Factory::create(DatabaseSeeder::$seedLanguage);

        $this->cleanStorageFolder('photos_avatars');

        $this->addUsersToDatabase();
        $this->addPhotoFiles();
        DB::table('users')->where('type', 'A')->update(['nickname' => null]);
        self::$dbUsers = DB::table('users')->orderBy('id')->get();
        $this->command->line("Users Created Successfully.");
    }

    private function addUsersToDatabase()
    {
        $this->command->line("Adding users to the database");
        $usersAdded = Self::$fixedUsers;
        foreach (self::$userTypes as $userType => $totalUsers) {
            for ($i = 0; $i < $totalUsers; $i++) {
                $gender = null;
                $name = null;
                $email = null;
                $this->randomName($this->faker, $gender, $name, $email, false);
                $usersAdded[] = [
                    'type' => $userType,
                    'name' => $name,
                    'email' => $email,
                    'gender' => $gender,
                    'softdelete' => random_int(1, 20) == 1,
                ];
            }
        }

        $createdDate = DatabaseSeeder::$startDate->copy()->addMinutes(mt_rand(20000, 100000));
        foreach ($usersAdded as $key => $user) {
            $usersAdded[$key]['password'] = self::$hashPasword;
            $createdDate = $createdDate->copy()->addMinutes(mt_rand(10, 60));
            $usersAdded[$key]['created_at'] = $createdDate;
            $usersAdded[$key]['email_verified_at'] = $createdDate->copy()->addMinutes(mt_rand(1, 9));
            if (random_int(1, 7) > 1) {
                $usersAdded[$key]['updated_at'] = $createdDate->copy()->addMinutes(mt_rand(10, 10000));
            } else {
                $usersAdded[$key]['updated_at'] = $createdDate;
            }
            $usersAdded[$key]['nickname'] = $user['nickname'] ?? (($user['gender'] == 'M' ? 'Mickey' : 'Minnie') . ($key + 1));
            $usersAdded[$key]['blocked'] = false;
            $usersAdded[$key]['photo_avatar_filename'] = null;
            $usersAdded[$key]['coins_balance'] = 0;
            $usersAdded[$key]['deleted_at'] = null;
            if ($usersAdded[$key]['softdelete']) {
                $usersAdded[$key]['deleted_at'] = $createdDate->copy()->addMinutes(mt_rand(10001, 100000));
            }
        }

        $arrayToStore = [];
        foreach ($usersAdded as $user) {
            unset($user['gender']);
            unset($user['softdelete']);
            $arrayToStore[] = $user;

            if (count($arrayToStore) >= DatabaseSeeder::$dbInsertBlockSize) {
                DB::table('users')->insert($arrayToStore);
                $this->command->line("Created " . count($arrayToStore) . " users");
                $arrayToStore = [];
            }
        }
        if (count($arrayToStore) >= 1) {
            DB::table('users')->insert($arrayToStore);
            $this->command->line("Created " . count($arrayToStore) . " users");
        }

        $this->command->line("Total users created: " . DB::table('users')->count());
        self::$dbUsers = DB::table('users')->get();

        print_r(self::$dbUsers);
    }

    private function addPhotoFiles()
    {
        $this->command->line("Copying users' photos");
        $this->fillPhotoFilesNames();
        $total = count($this->files_M) + count($this->files_F);
        $sortedUsers = self::$dbUsers->sortBy(function (stdClass $user) {
            if ($user->id <= 10 ) {
                return $user->id;
            }
            return match($user->type) {
                'A' => 20 + $user->id,
                'P' => 1000 + $user->id,
                default => ($user->id < 50) ? 2000 + $user->id : 3000 + mt_rand(0, 100000),
            };
        });
        $i = 0;
        foreach($sortedUsers as $user) {
            // If the user is 'aluno@mail.com', skip the random photo assignment
            if ($user->email === 'aluno@mail.com') {
                continue;
            }
            $originalFilename = str_starts_with($user->nickname, 'Mickey') ? array_shift($this->files_M) : array_shift($this->files_F);
            if (!$originalFilename) {
                if ((count($this->files_M) == 0) && (count($this->files_F) == 0))
                    break;
            }
            if ($originalFilename) {
                $originalFilename = basename($originalFilename);
                $newFileName = $this->copyFileToStorage('photos', $originalFilename, 'photos_avatars', $user->id);
                $user->photo_avatar_filename = $newFileName;
                DB::table('users')->where('id', $user->id)->update(['photo_avatar_filename' => $user->photo_avatar_filename]);
                $i++;
                if ($i % 10 == 0) {
                    $this->command->line("User photo $i/$total copied");
                }
            }
        }
        $this->command->line("Total of $total user's photos were copied!");
        $this->directCopyFileToStorage('photos', 'anonymous.png',  'photos_avatars');
        $this->command->line("Image for user with no associated photo was copied!");
    }

    private function fillPhotoFilesNames()
    {
        $allFiles = collect(File::files(database_path('seeders/photos')));
        foreach ($allFiles as $f) {
            if (is_object($f) && method_exists($f, 'getPathname') && strpos($f->getPathname(), 'm_') !== false) {
                $this->files_M[] = $f->getPathname();
            } else if (is_object($f) && method_exists($f, 'getPathname') && strpos($f->getPathname(), 'w_') !== false) {
                $this->files_F[] = $f->getPathname();
            }
        }
        shuffle($this->files_M);
        shuffle($this->files_F);
    }
}
