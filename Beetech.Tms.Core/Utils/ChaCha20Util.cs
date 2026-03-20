using System;
using System.Text;
using CSChaCha20;
using Force.Crc32;

namespace Beetech.Tms.Core.Utils
{
    public class ChaCha20Util
    {
        private static readonly byte[] KEY = Encoding.UTF8.GetBytes("this_is_a_32_byte_secret_key____");
        private static readonly byte[] NONCE = Encoding.UTF8.GetBytes("123456789012");

        public static string Encrypt(long number)
        {
            byte[] inputBytes = new byte[12];
            Array.Copy(BitConverter.GetBytes(number), inputBytes, 8);
            Array.Copy(BitConverter.GetBytes(DateTime.UtcNow.Ticks % 10000), 0, inputBytes, 8, 4);

            byte[] outputBytes = new byte[inputBytes.Length];

            using (ChaCha20 chacha = new ChaCha20(KEY, NONCE, 0))
            {
                chacha.EncryptBytes(outputBytes, inputBytes, inputBytes.Length, SimdMode.AutoDetect);
            }

            return BitConverter.ToString(outputBytes).Replace("-", "");
        }

        public static long Decrypt(string hexData)
        {
            try
            {
                byte[] encryptedBytes = HexToBytes(hexData);
                byte[] decryptedBytes = new byte[encryptedBytes.Length];

                using (ChaCha20 chacha = new ChaCha20(KEY, NONCE, 0))
                {
                    chacha.DecryptBytes(decryptedBytes, encryptedBytes, SimdMode.AutoDetect);
                }

                return BitConverter.ToInt64(decryptedBytes, 0);
            }
            catch
            {
                return -1;
            }
        }

        // --- Encrypt: returns 24 hex chars (4 type + 20 encrypted) ---
        public static string EncryptExt(long number, string typeHex)
        {
            if (string.IsNullOrWhiteSpace(typeHex) || typeHex.Length != 4)
                throw new ArgumentException("Type must be 4 hex digits");

            // Layout: [8 bytes number][2 bytes salt] = 10 bytes
            byte[] input = new byte[10];
            Array.Copy(BitConverter.GetBytes(number), 0, input, 0, 8);

            // Salt derived deterministically from the number itself
            ushort salt = (ushort)(number & 0xFFFF);
            Array.Copy(BitConverter.GetBytes(salt), 0, input, 8, 2);

            byte[] output = new byte[input.Length];
            using (var chacha = new ChaCha20(KEY, NONCE, 0))
            {
                chacha.EncryptBytes(output, input, input.Length, SimdMode.AutoDetect);
            }

            // 10 bytes → 20 hex chars, prefix with type
            return typeHex.ToUpperInvariant() + BytesToHex(output);
        }

        // --- Decrypt: split prefix and payload ---
        public static (long id, string? type) DecryptExt(string code)
        {
            try
            {
                if (string.IsNullOrWhiteSpace(code) || code.Length != 24)
                    return (-1L, null);

                string typeHex = code.Substring(0, 4);
                string hexData = code.Substring(4);

                byte[] encrypted = HexToBytes(hexData);
                byte[] decrypted = new byte[encrypted.Length];

                using (var chacha = new ChaCha20(KEY, NONCE, 0))
                {
                    chacha.DecryptBytes(decrypted, encrypted, SimdMode.AutoDetect);
                }

                long number = BitConverter.ToInt64(decrypted, 0);
                ushort salt = BitConverter.ToUInt16(decrypted, 8);

                // ✅ Validation: salt must equal (number & 0xFFFF)
                if (salt != (ushort)(number & 0xFFFF))
                    return (-1L, null);

                return (number, typeHex);
            }
            catch
            {
                return (-1L, null);
            }
        }

        public static string EncryptString(string plainText)
        {
            byte[] inputBytes = Encoding.UTF8.GetBytes(plainText);
            byte[] outputBytes = new byte[inputBytes.Length];

            using (ChaCha20 chacha = new ChaCha20(KEY, NONCE, 0))
            {
                chacha.EncryptBytes(outputBytes, inputBytes, inputBytes.Length, SimdMode.AutoDetect);
            }

            return BitConverter.ToString(outputBytes).Replace("-", "");
        }

        public static string? DecryptString(string hexData)
        {
            try
            {
                byte[] encryptedBytes = HexToBytes(hexData);
                byte[] decryptedBytes = new byte[encryptedBytes.Length];

                using (ChaCha20 chacha = new ChaCha20(KEY, NONCE, 0))
                {
                    chacha.DecryptBytes(decryptedBytes, encryptedBytes, SimdMode.AutoDetect);
                }

                return Encoding.UTF8.GetString(decryptedBytes);
            }
            catch
            {
                return null;
            }
        }

        // --- Utility: hex encode/decode ---
        private static string BytesToHex(byte[] bytes)
        {
            var sb = new StringBuilder(bytes.Length * 2);
            foreach (var b in bytes)
                sb.AppendFormat("{0:X2}", b);
            return sb.ToString();
        }

        private static byte[] HexToBytes(string hex)
        {
            byte[] data = new byte[hex.Length / 2];
            for (int i = 0; i < hex.Length; i += 2)
            {
                data[i / 2] = Convert.ToByte(hex.Substring(i, 2), 16);
            }
            return data;
        }
    }
}
