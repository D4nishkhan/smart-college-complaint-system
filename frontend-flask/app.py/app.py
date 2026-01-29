from flask import Flask, render_template, request, redirect
import requests

app = Flask(
    __name__,
    template_folder=r"C:\smart-college\frontend-flask\templates"
)

# -------------------------
# HOME
# -------------------------
@app.route("/")
def home():
    return render_template("base.html")

# -------------------------
# DEPARTMENTS
# -------------------------
@app.route("/departments", methods=["GET", "POST"])
def departments():
    if request.method == "POST":
        name = request.form.get("name")
        if name:
            requests.post(
                "http://localhost:8080/api/departments",
                json={"name": name}
            )
        return redirect("/departments")

    dept_list = requests.get(
        "http://localhost:8080/api/departments"
    ).json()

    return render_template(
        "departments.html",
        departments=dept_list
    )

# -------------------------
# STUDENTS
# -------------------------
@app.route("/students", methods=["GET", "POST"])
def students():
    try:
        departments = requests.get(
            "http://localhost:8080/api/departments"
        ).json()

        if request.method == "POST":
            name = request.form.get("name")
            email = request.form.get("email")
            dept_id = request.form.get("department_id")

            if name and email and dept_id:
                data = {
                    "name": name,
                    "email": email,
                    "department": {
                        "id": int(dept_id)
                    }
                }

                requests.post(
                    "http://localhost:8080/api/students",
                    json=data
                )

            return redirect("/students")

        students_list = requests.get(
            "http://localhost:8080/api/students"
        ).json()

        return render_template(
            "students.html",
            students=students_list,
            departments=departments
        )

    except Exception as e:
        return f"STUDENTS ROUTE ERROR: {e}"

# -------------------------
# COMPLAINTS (STUDENT)
# -------------------------
@app.route("/complaints", methods=["GET", "POST"])
def complaints():
    departments = requests.get(
        "http://localhost:8080/api/departments"
    ).json()

    if request.method == "POST":
        title = request.form.get("title")
        description = request.form.get("description")
        dept_id = request.form.get("department_id")

        if title and description and dept_id:
            data = {
                "title": title,
                "description": description,
                "department": {
                    "id": int(dept_id)
                }
            }

            requests.post(
                "http://localhost:8080/api/complaints",
                json=data
            )

        return redirect("/complaints")

    complaints_list = requests.get(
        "http://localhost:8080/api/complaints"
    ).json()

    return render_template(
        "complaints.html",
        complaints=complaints_list,
        departments=departments
    )

# -------------------------
# ADMIN (DEPARTMENT STAFF)
# -------------------------
@app.route("/admin/complaints", methods=["GET", "POST"])
def admin_complaints():
    if request.method == "POST":
        complaint_id = request.form.get("complaint_id")
        status = request.form.get("status")

        if complaint_id and status:
            requests.put(
                f"http://localhost:8080/api/complaints/{complaint_id}/status",
                params={"status": status}
            )

        return redirect("/admin/complaints")

    complaints = requests.get(
        "http://localhost:8080/api/complaints"
    ).json()

    return render_template(
        "admin_complaints.html",
        complaints=complaints
    )

# -------------------------
# SUPER ADMIN (PRINCIPAL)
# -------------------------
@app.route("/superadmin/dashboard")
def superadmin_dashboard():
    complaints = requests.get(
        "http://localhost:8080/api/complaints"
    ).json()

    return render_template(
        "superadmin_dashboard.html",
        complaints=complaints
    )

# -------------------------
# RUN
# -------------------------
if __name__ == "__main__":
    app.run(debug=True)
