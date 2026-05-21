function selectCurrentOption(selectId, hiddenId) {
    const select = document.getElementById(selectId);
    const hidden = document.getElementById(hiddenId);
    if (!select || !hidden || !hidden.value) {
        return;
    }

    Array.from(select.options).forEach((option) => {
        if (option.value === hidden.value) {
            option.selected = true;
        }
    });
}

function validatePatientForm(form) {
    const name = form.name.value.trim();
    const age = Number(form.age.value);
    const phone = form.phone.value.trim();
    const address = form.address.value.trim();
    const ward = form.ward.value.trim();

    if (name.length < 3) {
        alert("Patient name must be at least 3 characters long.");
        form.name.focus();
        return false;
    }

    if (!Number.isInteger(age) || age < 0 || age > 120) {
        alert("Enter a valid patient age between 0 and 120.");
        form.age.focus();
        return false;
    }

    const phoneRegex = /^[6-9]\d{9}$/;
    if (!phoneRegex.test(phone)) {
        alert("Phone number must be a valid 10-digit mobile number starting with 6, 7, 8, or 9.");
        form.phone.focus();
        return false;
    }

    if (address.length < 3) {
        alert("Address must be at least 3 characters long.");
        form.address.focus();
        return false;
    }

    if (ward.length < 2) {
        alert("Ward must be at least 2 characters long.");
        form.ward.focus();
        return false;
    }

    if (!form.gender.value || !form.blood.value || !form.doctorId.value || !form.status.value) {
        alert("Please select gender, blood group, doctor, and admission status.");
        return false;
    }

    return true;
}

document.addEventListener("DOMContentLoaded", () => {
    const alertBox = document.querySelector(".alert-box");
    if (alertBox) {
        setTimeout(() => {
            alertBox.style.opacity = "0";
            alertBox.style.transition = "opacity 0.6s ease-out";
            setTimeout(() => {
                alertBox.style.display = "none";
            }, 600);
        }, 5000);
    }
});
