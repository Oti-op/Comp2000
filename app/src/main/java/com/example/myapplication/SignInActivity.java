package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.inputmethod.EditorInfo;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

/** S1 Sign in. There is no backend, so any well-formed email and a password are accepted. */
public class SignInActivity extends AppCompatActivity {

    private TextInputLayout emailLayout;
    private TextInputLayout passwordLayout;
    private TextInputEditText email;
    private TextInputEditText password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_in);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars()
                    | WindowInsetsCompat.Type.displayCutout() | WindowInsetsCompat.Type.ime());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return WindowInsetsCompat.CONSUMED;
        });

        emailLayout = findViewById(R.id.email_layout);
        passwordLayout = findViewById(R.id.password_layout);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);

        findViewById(R.id.sign_in).setOnClickListener(v -> attemptSignIn());
        password.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                attemptSignIn();
                return true;
            }
            return false;
        });
    }

    private void attemptSignIn() {
        String emailText = email.getText() == null ? "" : email.getText().toString().trim();
        String passwordText = password.getText() == null ? "" : password.getText().toString();

        boolean emailOk = Patterns.EMAIL_ADDRESS.matcher(emailText).matches();
        boolean passwordOk = !TextUtils.isEmpty(passwordText);
        // Errors sit under the field they belong to and say how to fix it.
        emailLayout.setError(emailOk ? null : getString(R.string.error_email));
        passwordLayout.setError(passwordOk ? null : getString(R.string.error_password));

        if (!emailOk) {
            email.requestFocus();
        } else if (!passwordOk) {
            password.requestFocus();
        } else {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }
}
