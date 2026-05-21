# Hospital Patient Records - Netlify Version

This folder contains a Netlify-ready static version of the Hospital Patient Records project.

The original project uses Java Servlets, Tomcat, XAMPP and MySQL. Netlify cannot run that backend directly, so this version runs fully in the browser and stores demo records in `localStorage`.

## Preview

Run:

```bat
preview_local.bat
```

Then open:

```text
http://127.0.0.1:4177
```

## Deploy

Login once:

```bat
npx --yes netlify-cli@latest login
```

Deploy:

```bat
deploy_to_netlify.bat
```

## Included Features

- Register patient
- Patient issue field
- Assign doctor by department
- Appointment date and notes
- Search by name, ID, status, department, natural phrases
- View patient details
- Edit patient
- Discharge patient
- Delete patient
- AI health summary
- AI risk prediction
- AI smart search
- AI emergency priority detection
- Analytics dashboard
