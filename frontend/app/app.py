from flask import Flask, render_template, send_from_directory, url_for, request, redirect
from flask_login import LoginManager, login_manager, current_user, login_user, login_required, logout_user
import requests
import os
import logging

# Usuarios
from models import users, User

# Login
from forms import LoginForm, RegistrationForm

# Workaround: Login
import hashlib

# Configuración del logger
logging.basicConfig(format="%(filename)s:%(lineno)d - %(levelname)s - %(message)s")
logging.getLogger().setLevel(logging.INFO)

app = Flask(__name__, static_url_path='')
login_manager = LoginManager()
login_manager.init_app(app) # Para mantener la sesión

# Configurar el secret_key. OJO, no debe ir en un servidor git público.
# Python ofrece varias formas de almacenar esto de forma segura, que
# no cubriremos aquí.
app.config['SECRET_KEY'] = 'qH1vprMjavek52cv7Lmfe1FoCexrrV8egFnB21jHhkuOHm8hJUe1hwn7pKEZQ1fioUzDb3sWcNK1pJVVIhyrgvFiIrceXpKJBFIn_i9-LTLBCc4cqaI3gjJJHU6kxuT8bnC7Ng'

@app.route('/static/<path:path>')
def serve_static(path):
    return send_from_directory('static', path)

@app.route('/', methods=['GET', 'POST'])
def index():
    if request.method == "POST":
        logging.info('REQUEST DATA: ' + request.form['textarea'])
        # Falta la lógica para mandar al REST 
        pass
    return render_template('index.html')

@app.route('/login', methods=['GET', 'POST'])
def login():
    if current_user.is_authenticated:
        return redirect(url_for('index'))
    else:
        error = None
        form = LoginForm(None if request.method != 'POST' else request.form)
        if request.method == "POST" and form.validate():
            loginData = {"email": form.email.data, "password": form.password.data}
            loginPOST = requests.post('http://backend-rest:8080/Service/checkLogin', json=loginData)
            if loginPOST.status_code == 200:
                json = loginPOST.json()
                logging.info(json)
                # Esto es un workaround. Este parseo a int debería de hacerse al registrarse
                user = User(int(hashlib.md5(json['id'].encode()).hexdigest(), 16), json['name'], json['email'], form.password.data.encode('utf-8'), True if json['name'] == "diego" else False)
                # user = User(json['id'], json['name'], json['email'], form.password.data.encode('utf-8'))
                users.append(user)
                login_user(user, remember=form.remember_me.data)
                return redirect(url_for('index'))
            else:
                error = "Credenciales incorrectas. Vuelve a intentarlo"
        return render_template('login.html', form=form,  error=error)
    
@app.route('/signup', methods=['GET', 'POST'])
def register():
    if current_user.is_authenticated:
        return redirect(url_for('index'))
    else:
        error = None
        form = RegistrationForm(None if request.method != 'POST' else request.form)
        if request.method == "POST" and form.validate():
            user = User()
        return render_template('signup.html', form=form, error=error)

@app.route('/profile')
@login_required
def profile():
    return render_template('profile.html')

@app.route('/logout')
@login_required
def logout():
    logout_user()
    return redirect(url_for('index'))

@login_manager.user_loader
def load_user(user_id):
    for user in users:
        if user.id == int(user_id):
            return user
    return None

if __name__ == '__main__':
    app.run(debug=True, host='0.0.0.0', port=int(os.environ.get('PORT', 5010)))
