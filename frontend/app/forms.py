from flask_wtf import FlaskForm
from wtforms import (StringField, PasswordField, BooleanField, FileField, EmailField, SubmitField)
from wtforms.validators import InputRequired, Length, Email, DataRequired

class LoginForm(FlaskForm):
    email = StringField('email', validators=[Email()])
    password = PasswordField('password', validators=[InputRequired()])
    remember_me = BooleanField('remember_me')

class RegistrationForm(FlaskForm):
    name = StringField('name', validators=[DataRequired(), Length(min=2, max=20)])
    email = EmailField('email', validators=[DataRequired(), Email()])
    password = PasswordField('password', validators=[InputRequired(), DataRequired()])