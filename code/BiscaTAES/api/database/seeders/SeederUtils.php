<?php

namespace Database\Seeders;

use Illuminate\Support\Facades\File;

trait SeederUtils
{
    private static $used_emails = [];

    public function randomString(int $length = 10): string
    {
        return substr(str_shuffle('abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789'), 0, $length);
    }

    public function integerAsStringWithPadding(int $num, int $length = 10): string
    {
        return str_pad((string) $num, $length, '0', STR_PAD_LEFT);
    }

    public function randomIntegerAsStringWithPadding(int $min = 0, int $max = 100, int $length = 10): string
    {
        return $this->integerAsStringWithPadding($this->mt_rand($min, $max), $length);
    }

    public function randomDecimal(float $min, float $max, int $decimals = 2): float
    {
        $factor = pow(10, $decimals);
        return mt_rand($min * $factor, $max * $factor) / $factor;
    }


    public function getFileNameFromString($str, $extension)
    {
        return str_replace(' ', '_', strtolower($this->stripAccents($str))) . ".$extension";
    }

    public function stripAccents($stripAccents)
    {
        $from = 'àáâãäçèéêëìíîïñòóôõöùúûüýÿÀÁÂÃÄÇÈÉÊËÌÍÎÏÑÒÓÔÕÖÙÚÛÜÝ';
        $to =   'aaaaaceeeeiiiinooooouuuuyyAAAAACEEEEIIIINOOOOOUUUUY';
        $keys = array();
        $values = array();
        preg_match_all('/./u', $from, $keys);
        preg_match_all('/./u', $to, $values);
        $mapping = array_combine($keys[0], $values[0]);
        return strtr($stripAccents, $mapping);
    }

    public function strtr_utf8($str, $from, $to)
    {
        $keys = array();
        $values = array();
        preg_match_all('/./u', $from, $keys);
        preg_match_all('/./u', $to, $values);
        $mapping = array_combine($keys[0], $values[0]);
        return strtr($str, $mapping);
    }

    public function randomName($faker, &$gender, &$fullname, &$email, $allowRepeated = false)
    {
        $gender = $gender ?? $faker->randomElement(['male', 'female']);
        $firstname = $faker->firstName($gender);
        $lastname = $faker->lastName();
        $secondname = mt_rand(1, 3) == 2 ? "" : " " . $faker->firstName($gender);
        $number_middlenames = $faker->randomElement([0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 3]);
        $middlenames = "";
        for ($i = 0; $i < $number_middlenames; $i++) {
            $middlenames .= " " . $faker->lastName();
        }
        $fullname = $firstname . $secondname . $middlenames . " " . $lastname;
        $email = strtolower($this->stripAccents($firstname) . "." . $this->stripAccents($lastname) . "@mail.pt");
        if (!$allowRepeated) {
            $i = 2;
            while (in_array($email, self::$used_emails)) {
                $email = strtolower($this->stripAccents($firstname) . "." . $this->stripAccents($lastname) . "." . $i ."@mail.pt");
                $i++;
                if ($i > 10) {
                    $this->command->error("Repeated email - not possible to create a unique email for $fullname!");
                    exit(1);
                }
            }
        }
        self::$used_emails[] = $email;
        $gender = $gender == 'male' ? 'M' : 'F';
    }

    public function ramdomPaymentMethod($email, &$paymentType, &$paymentReference)
    {
        $paymentType = $this->faker->randomElement(['Visa', 'PayPal', 'MB WAY']);
        $paymentReference = match ($paymentType) {
            'Visa' => mt_rand(4, 6) .
                $this->faker->randomNumber($nbDigits = 8, $strict = true) .
                $this->faker->randomNumber($nbDigits = 7, $strict = true),
            'PayPal' => $email,
            'MB WAY' => '9' . $this->faker->randomNumber($nbDigits = 8, $strict = true)
        };
    }

    public function cleanStorageFolder($folder, $public = true)
    {
        $storagePath = $public ? storage_path("app/public/$folder") : storage_path("app/private/$folder");
        if (File::exists($storagePath)) {
            File::deleteDirectory($storagePath);
        }
        if (!File::exists(storage_path("app"))) {
            File::makeDirectory(storage_path("app"));
        }
        if ($public) {
            if (!File::exists(storage_path("app/public"))) {
                File::makeDirectory(storage_path("app/public"));
            }
        } else {
            if (!File::exists(storage_path("app/private"))) {
                File::makeDirectory(storage_path("app/private"));
            }
        }
        File::makeDirectory($storagePath);
    }


    public function copyFileToStorage($originalFolder, $originalFileName, $folder, $id = null, $public = true)
    {
        $originalFullName = database_path("seeders/$originalFolder") . '/' . $originalFileName;
        $storagePath = $public ? storage_path("app/public/$folder") : storage_path("app/private/$folder");
        $prefix = $id ? $this->integerAsStringWithPadding($id, 5) . '_' : '';
        $ext = pathinfo($originalFullName, PATHINFO_EXTENSION);
        $newFileName =  $prefix . $this->randomString(10) . '.' . $ext;
        if (!File::exists($storagePath)) {
            File::makeDirectory($storagePath);
        }
        if (File::exists($originalFullName)) {
            File::copy($originalFullName, "$storagePath/$newFileName");
            return $newFileName;
        }
        return null;
    }

    public function directCopyFileToStorage($originalFolder, $originalFileName, $folder, $public = true)
    {
        $originalFullName = database_path("seeders/$originalFolder") . '/' . $originalFileName;
        $storagePath = $public ? storage_path("app/public/$folder") : storage_path("app/private/$folder");
        if (!File::exists($storagePath)) {
            File::makeDirectory($storagePath);
        }
        File::copy($originalFullName, "$storagePath/$originalFileName");
        return $originalFileName;
    }
}
