from flask import Flask, Blueprint, render_template, request, redirect, url_for, session
import requests
import os

# ===============================
# CONFIG
# ===============================
BACKEND_BASE = os.getenv("BACKEND_BASE", "https://smart-college-backend.onrender.com")
FLASK_SECRET = os.getenv("FLASK_SECRET", "smart-college-dev-secret")


# ===============================
# BACKEND HTTP HELPERS
# ===============================
def backend_get(path, headers=None):
    return requests.get(f"{BACKEND_BASE}{path}", headers=headers, timeout=90)


def backend_post(path, json=None, headers=None):
    return requests.post(f"{BACKEND_BASE}{path}", json=json, headers=headers, timeout=90)


def backend_put(path, params=None, headers=None):
    return requests.put(f"{BACKEND_BASE}{path}", params=params, headers=headers, timeout=90)


def err_msg(resp):
    """
    Hide backend JSON/timestamps. Show only user-friendly message.
    Works with your ApiError JSON that includes { message, timestamp, ... }.
    """
    try:
        j = resp.json()
        if isinstance(j, dict):
            return j.get("message") or resp.text
        return str(j)
    except:
        return resp.text


def set_msg(current, new_msg):
    # keep first important message, don’t overwrite repeatedly
    return current if current else new_msg


# ===============================
# SESSION HELPERS
# ===============================
def require_student_key():
    return session.get("studentKey")


def require_teacher_key():
    return session.get("teacherKey")


def require_principal_creds():
    u = session.get("pUser")
    s = session.get("pSecret")
    if not u or not s:
        return None
    return (u, s)


# ===============================
# BLUEPRINTS
# ===============================
main_bp = Blueprint("main", __name__)
student_bp = Blueprint("student", __name__, url_prefix="/student")
teacher_bp = Blueprint("teacher", __name__, url_prefix="/teacher")
principal_bp = Blueprint("principal", __name__, url_prefix="/principal")


# ===============================
# MAIN
# ===============================
@main_bp.route("/")
def index():
    return render_template("index.html", backend_base=BACKEND_BASE)


# ===============================
# STUDENT
# ===============================
@student_bp.route("", methods=["GET"])
@student_bp.route("/", methods=["GET"])
def student_choice():
    return render_template("student_choice.html", backend_base=BACKEND_BASE)


@student_bp.route("/register", methods=["GET", "POST"])
def student_register():
    msg = None
    depts = []

    # Load departments (principal must add first)
    try:
        r = backend_get("/api/departments")
        if r.ok:
            depts = r.json()
        else:
            msg = set_msg(msg, err_msg(r))
    except Exception as e:
        msg = set_msg(msg, f"Backend not reachable: {e}")

    if request.method == "POST":
        if not depts:
            msg = set_msg(msg, "No departments available. Ask Principal to add departments first.")
            return render_template("student_register.html", backend_base=BACKEND_BASE, msg=msg, depts=depts)

        try:
            body = {
                "name": request.form.get("name", ""),
                "email": request.form.get("email", ""),
                "password": request.form.get("password", ""),
                "departmentId": int(request.form.get("departmentId"))
            }
            r = backend_post("/student/register", json=body)
            if r.ok:
                data = r.json()
                session["studentKey"] = data.get("apiKey")
                session["studentId"] = data.get("id")
                return redirect(url_for("student.student_dashboard"))
            msg = set_msg(msg, err_msg(r))
        except Exception as e:
            msg = set_msg(msg, f"Backend not reachable: {e}")

    return render_template("student_register.html", backend_base=BACKEND_BASE, msg=msg, depts=depts)


@student_bp.route("/login", methods=["GET", "POST"])
def student_login():
    msg = None

    if request.method == "POST":
        try:
            body = {
                "email": request.form.get("email", ""),
                "password": request.form.get("password", "")
            }
            r = backend_post("/student/login", json=body)
            if r.ok:
                data = r.json()
                session["studentKey"] = data.get("apiKey")
                session["studentId"] = data.get("id")
                return redirect(url_for("student.student_dashboard"))
            msg = set_msg(msg, "Invalid email or password.")
        except Exception as e:
            msg = set_msg(msg, f"Backend not reachable: {e}")

    return render_template("student_login.html", backend_base=BACKEND_BASE, msg=msg)


@student_bp.route("/dashboard", methods=["GET", "POST"])
def student_dashboard():
    key = require_student_key()
    if not key:
        return redirect(url_for("student.student_login"))

    msg = None
    created = False
    complaints = []

    # Create complaint
    if request.method == "POST":
        try:
            body = {
                "title": request.form.get("title", ""),
                "description": request.form.get("description", "")
            }
            r = backend_post("/student/me/complaints", json=body, headers={"X-STUDENT-KEY": key})
            if r.ok:
                created = True
            else:
                msg = set_msg(msg, err_msg(r))
        except Exception as e:
            msg = set_msg(msg, f"Backend not reachable: {e}")

    # Load complaints
    try:
        r = backend_get("/student/me/complaints", headers={"X-STUDENT-KEY": key})
        if r.ok:
            complaints = r.json()
        else:
            msg = set_msg(msg, err_msg(r))
    except Exception as e:
        msg = set_msg(msg, f"Backend not reachable: {e}")

    return render_template("student_dashboard.html",
                           backend_base=BACKEND_BASE,
                           msg=msg,
                           created=created,
                           complaints=complaints)


@student_bp.route("/logout")
def student_logout():
    session.pop("studentKey", None)
    session.pop("studentId", None)
    return redirect(url_for("student.student_login"))


# ===============================
# TEACHER
# ===============================
@teacher_bp.route("", methods=["GET"])
@teacher_bp.route("/", methods=["GET"])
def teacher_choice():
    return render_template("teacher_choice.html", backend_base=BACKEND_BASE)


@teacher_bp.route("/register", methods=["GET", "POST"])
def teacher_register():
    msg = None
    okmsg = None

    # IMPORTANT: register page always shows form
    # dashboard redirect only happens after successful login

    if request.method == "POST":
        try:
            body = {
                "name": request.form.get("name", ""),
                "email": request.form.get("email", ""),
                "password": request.form.get("password", "")
            }
            r = backend_post("/teacher/register", json=body)
            if r.ok:
                okmsg = "Request submitted successfully. Wait for Principal approval."
            else:
                msg = set_msg(msg, err_msg(r))
        except Exception as e:
            msg = set_msg(msg, f"Backend not reachable: {e}")

    return render_template("teacher_register.html", backend_base=BACKEND_BASE, msg=msg, okmsg=okmsg)


@teacher_bp.route("/login", methods=["GET", "POST"])
def teacher_login():
    msg = None

    if request.method == "POST":
        try:
            body = {
                "email": request.form.get("email", ""),
                "password": request.form.get("password", "")
            }
            r = backend_post("/teacher/login", json=body)
            if r.ok:
                data = r.json()
                session["teacherKey"] = data.get("apiKey")
                session["teacherId"] = data.get("id")
                return redirect(url_for("teacher.teacher_dashboard"))

            # show friendly message (no JSON)
            msg = err_msg(r)
            # normalize common backend messages:
            if "not approved" in (msg or "").lower():
                msg = "Your request is not approved by the Principal yet."
            elif "department not assigned" in (msg or "").lower():
                msg = "Your department is not assigned by the Principal yet."
        except Exception as e:
            msg = set_msg(msg, f"Backend not reachable: {e}")

    return render_template("teacher_login.html", backend_base=BACKEND_BASE, msg=msg)


@teacher_bp.route("/dashboard")
def teacher_dashboard():
    key = require_teacher_key()
    if not key:
        return redirect(url_for("teacher.teacher_login"))

    msg = None
    teacher_info = None
    department_name = ""
    students_all = []
    student_count_total = 0
    complaints = []

    try:
        r = backend_get("/teacher/me/info", headers={"X-TEACHER-KEY": key})
        if r.ok:
            teacher_info = r.json()
            department_name = teacher_info.get("departmentName", "")
        else:
            msg = set_msg(msg, err_msg(r))
    except Exception as e:
        msg = set_msg(msg, f"Backend not reachable: {e}")

    # ✅ all students (name + department)
    try:
        r = backend_get("/teacher/me/students/all", headers={"X-TEACHER-KEY": key})
        if r.ok:
            students_all = r.json()
            student_count_total = len(students_all)
        else:
            msg = set_msg(msg, err_msg(r))
    except Exception as e:
        msg = set_msg(msg, f"Backend not reachable: {e}")

    # dept complaints only
    try:
        r = backend_get("/teacher/me/complaints", headers={"X-TEACHER-KEY": key})
        if r.ok:
            complaints = r.json()
        else:
            msg = set_msg(msg, err_msg(r))
    except Exception as e:
        msg = set_msg(msg, f"Backend not reachable: {e}")

    return render_template("teacher_dashboard.html",
                           backend_base=BACKEND_BASE,
                           msg=msg,
                           teacher_info=teacher_info,
                           department_name=department_name,
                           student_count_total=student_count_total,
                           students_all=students_all,
                           complaints=complaints)
@teacher_bp.route("/logout")
def teacher_logout():
    session.pop("teacherKey", None)
    session.pop("teacherId", None)
    return redirect(url_for("teacher.teacher_login"))


# ===============================
# PRINCIPAL
# ===============================
@principal_bp.route("", methods=["GET", "POST"])
@principal_bp.route("/", methods=["GET", "POST"])
def principal_login():
    if request.method == "POST":
        session["pUser"] = request.form.get("username", "")
        session["pSecret"] = request.form.get("secret", "")
        return redirect(url_for("principal.principal_dashboard"))
    return render_template("principal_login.html", backend_base=BACKEND_BASE)


@principal_bp.route("/dashboard", methods=["GET", "POST"])
def principal_dashboard():
    creds = require_principal_creds()
    if not creds:
        return redirect(url_for("principal.principal_login"))

    pUser, pSecret = creds
    headers = {"X-PRINCIPAL-USERNAME": pUser, "X-PRINCIPAL-SECRET": pSecret}

    msg = None
    pending = []
    unassigned = []
    depts = []
    complaints = []

    # Add department (POST) — only principal can add (backend also checks headers)
    if request.method == "POST":
        dept_name = (request.form.get("dept_name") or "").strip()
        try:
            r = backend_post("/api/departments", json={"name": dept_name}, headers=headers)
            msg = set_msg(msg, "Department added successfully." if r.ok else err_msg(r))
        except Exception as e:
            msg = set_msg(msg, f"Backend not reachable: {e}")

    # Actions (GET)
    action = request.args.get("action")
    try:
        if action == "approve":
            teacher_id = request.args.get("teacherId")
            approved = request.args.get("approved")
            r = backend_put(f"/principal/teachers/{teacher_id}/approve",
                            params={"approved": approved}, headers=headers)
            msg = set_msg(msg, "Teacher action completed." if r.ok else err_msg(r))

        if action == "assignDept":
            teacher_id = request.args.get("teacherId")
            dept_id = request.args.get("deptId")
            r = backend_put(f"/principal/teachers/{teacher_id}/department/{dept_id}", headers=headers)
            msg = set_msg(msg, "Department assigned successfully." if r.ok else err_msg(r))

        if action == "status":
            complaint_id = request.args.get("complaintId")
            status = request.args.get("status")
            r = backend_put(f"/principal/complaints/{complaint_id}/status",
                            params={"status": status}, headers=headers)
            msg = set_msg(msg, "Complaint status updated successfully." if r.ok else err_msg(r))
    except Exception as e:
        msg = set_msg(msg, f"Backend not reachable: {e}")

    # Load departments list
    try:
        r = backend_get("/api/departments")
        if r.ok:
            depts = r.json()
        else:
            msg = set_msg(msg, err_msg(r))
    except Exception as e:
        msg = set_msg(msg, f"Backend not reachable: {e}")

    # Pending teachers (verified=false)
    try:
        r = backend_get("/principal/teachers/pending", headers=headers)
        if r.ok:
            pending = r.json()
        else:
            msg = set_msg(msg, err_msg(r))
    except Exception as e:
        msg = set_msg(msg, f"Backend not reachable: {e}")

    # Approved but unassigned teachers (verified=true & dept=null)
    try:
        r = backend_get("/principal/teachers/unassigned", headers=headers)
        if r.ok:
            unassigned = r.json()
        else:
            msg = set_msg(msg, err_msg(r))
    except Exception as e:
        msg = set_msg(msg, f"Backend not reachable: {e}")

    # ✅ All complaints (principal view)
    try:
        r = backend_get("/principal/complaints", headers=headers)
        if r.ok:
            complaints = r.json()
        else:
            msg = set_msg(msg, err_msg(r))
    except Exception as e:
        msg = set_msg(msg, f"Backend not reachable: {e}")

    return render_template("principal_dashboard.html",
                           backend_base=BACKEND_BASE,
                           msg=msg,
                           pending=pending,
                           unassigned=unassigned,
                           depts=depts,
                           complaints=complaints)


@principal_bp.route("/logout")
def principal_logout():
    session.pop("pUser", None)
    session.pop("pSecret", None)
    return redirect(url_for("principal.principal_login"))


# ===============================
# APP FACTORY
# ===============================
def create_app():
    flask_app = Flask(__name__)
    flask_app.secret_key = FLASK_SECRET

    flask_app.register_blueprint(main_bp)
    flask_app.register_blueprint(student_bp)
    flask_app.register_blueprint(teacher_bp)
    flask_app.register_blueprint(principal_bp)

    return flask_app


app = create_app()

if __name__ == "__main__":
    app.run(debug=True, port=5000)