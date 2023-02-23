#Syntax for the ReAuth Toml configuration files
A ReAuth Toml file (v3) contains a list of saved Profiles.

A single configuration file may be shared across multiple instances/modpacks using [symlinks](https://en.wikipedia.org/wiki/Symbolic_link).
Please refer to documentation on how to create symlinks on your operating system.
Windows shortcuts are unsupported, please use symlinks instead.  
Copying or moving the file to a new location renders stored profiles unusable.

While multiple Profiles are supported by the configuration syntax, only the first profile is currently used. 
Support for multiple profiles *may* be added in a future version of ReAuth.

##Fields
The profile list uses Toml's [Array of Tables](https://toml.io/en/v1.0.0-rc.3#array-of-tables) syntax.
Each `profiles` entry contains multiple required and optional fields.

If a required field is missing or contains a non-permissible value the Profile is invalid.
To allow forwards compatibility, an invalid Profile cannot be edited or used within the Game.

If an optional field is missing or contains a non-permissible value, a default value is assumed.

Empty profile entries will be deleted.
If the list of profiles is empty, a placeholder entry is inserted.

###Type declaration
Each entry requires a field `type` denoting the type of profile.  
Known profile-types are:
* `microsoft` denoting a Microsoft profile
* `none` denoting a placeholder entry

###Microsoft Profiles
Microsoft profiles require the fields `refresh-token` and `xbl-token`.
The refresh token is used to acquire a new XBL-Token when needed.  
The XBL-Token is used in the authentication with Mojang.  
The contents of the token-fields are encrypted (see [Encryption](#Encryption)).
```toml
[[profiles]]
    type = "microsoft"
    refresh-token = "refresh-token"
    xbl-token = "xbl-token"
```

###Optional Fields
* Field `name` contains the last known name for that profile used for display.
* Field `uuid` contains the last known uuid for that profile used for determining which profile to offer when failing to join a server.

###Encryption
Some fields are encrypted using PBKDF2 and AES-256, and are encoded using Base64.
**While encryption prevents credentials from being read easily,
an attacker with sufficient access to your computer may still be able to extract the credentials.**

The specific algorithm in use is PBKDF2WithHmacSHA512 using 250,000 rounds and 16 bytes of cryptographic salt.
PBKDF2 is used to derive 64 bytes (512 bits) of key material.
* Bytes 00 to 31: Key for AES-256
* Bytes 32 to 47: IV for AES-256
* Bytes 48 to 63: IV for AES-256

In order to prevent cryptographic attacks based reusing a Key/IV pair:
* The individual salt value of each Profile leads to different encryption keys even if the encryption keys and content are identical.
* Encrypted fields have separate IVs.

The required Field `salt` contains the [cryptographic salt](https://en.wikipedia.org/wiki/Salt_(cryptography)) for the PBKDF2 algorithm (16 Bytes, Base64).
The mechanism used to derive the encryption key depends on the required field `key`:
* `auto` is used to denote the password being equivalent to the location of the config file.
  Symlinks are followed to allow the config to be shared across instances.
  Copying or moving the file renders profiles using this mechanism unusable until restored to their original location.
  The variation in the file path across users, launchers and operating systems should contain enough entropy for a secure password.
* `none` disables encryption for this profile.
  This is not recommended as sensitive fields are stored in plain.
  Fields required for PBKDF2 should be omitted.
```toml
[[profiles]]
    key = "auto"
    salt = "SH+75KX8l3EPBjYUHP/jIg=="
```

##Example Configuration
```toml
[[profiles]]
    type = "mojang"
    name = "Steve"
    uuid = "fba3fb36-63b4-45ed-92bf-8e0a1afc0f34"
    username = "encrypted-username"
    password = "encrypted-password"
    key = "auto"
    salt = "SH+75KX8l3EPBjYUHP/jIg=="

[[profiles]]
    type = "microsoft"
    name = "Alex"
    uuid = "c0365690-5c61-4cc5-b564-99e1ceae7709"
    refresh-token = "encrypted-refresh-token"
    xbl-token = "encrypted-xbl-token"
    key = "user"
    salt = "2nTI7nycdLlQn5xOTDNhkg=="
```

