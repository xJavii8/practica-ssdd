import requests
import sys
import os
import hashlib
import datetime

EXTERNAL_SERVICE_URL = "http://localhost:8180/ExternalService/"
userID = ""
userEmail = ""
username = ""
userToken = ""

def main():
    print('\nBienvenido al cliente del servicio REST externo de LlamaChat.')
    while True:
        print('Estado: \033[91mno autenticado\033[0m.')
        print('Opciones disponibles:')
        print('1. Registrarse')
        print('2. Iniciar sesión')
        print('3. Salir')
        choice = input("\nElige una opción: ")
        
        if choice == '1':
            register()
        elif choice == '2':
            login()
        elif choice == '3':
            print("\033[91mSaliendo...\033[0m")
            sys.exit()
        else:
            print("Opción \033[91mno válida\033[0m. Inténtalo de nuevo.\n")
        
        if userID != "":
            break
        
    clearScreen()
    print("\033[92mHas iniciado sesión correctamente. Bienvenido\033[0m")

    while True:
        print("Estado: \033[92mautenticado\033[0m.")
        print("Opciones disponibles:")
        print("1. Crear una conversación")
        print("2. Ver la lista de conversaciones")
        print("3. Ver mis datos y estadísticas")
        print("4. Eliminar mi usuario")
        print("5. Salir")

        choice = input("\nElige una opción: ")

        if choice == "1":
            createConv()
        elif choice == "2":
            checkConvList()
        elif choice == "3":
            checkUserData()
        elif choice == "4":
            deleteUser()
        elif choice == '5':
            print("\033[91mSaliendo...\033[0m")
            sys.exit()
        else:
            print("Opción \033[91mno válida\033[0m. Inténtalo de nuevo.\n")


def register():
    global userID
    global username
    global userEmail
    global userToken
    print("\nOpción elegida: \033[92mregistro\033[0m.")
    email = input("Correo electrónico: ")
    name = input("Nombre de usuario: ")
    password = input("Contraseña: ")
    json = {"email": email, "name": name, "password": password}
    response = requests.post(EXTERNAL_SERVICE_URL + "u/register", json=json)
    if response.status_code == 200:
        responseJSON = response.json()
        userID = responseJSON['id']
        username = responseJSON['name']
        userEmail = responseJSON['email']
        userToken = responseJSON['token']
    else:
        print("\033[91mHa ocurrido un error al hacer el registro. Por favor, inténtalo de nuevo\033[0m\n")

def login():
    global userID
    global username
    global userEmail
    global userToken
    print("\nOpción elegida: \033[92miniciar sesión\033[0m.")
    email = input("Correo electrónico: ")
    password = input("Contraseña: ")
    json = {"email": email, "password": password}
    response = requests.post(EXTERNAL_SERVICE_URL + "checkLogin", json=json)
    if response.status_code == 200:
        responseJSON = response.json()
        userID = responseJSON['id']
        username = responseJSON['name']
        userEmail = responseJSON['email']
        userToken = responseJSON['token']
    else:
        print("\033[91mHa ocurrido un error al iniciar sesión. Por favor, inténtalo de nuevo\033[0m\n")

def createConv():
    print("\nOpción elegida: \033[92mcrear una conversación\033[0m.")
    convName = input("Nombre de la conversación: ")

def checkConvList():
    print("\nOpción elegida: \033[92mver la lista de conversaciones\033[0m.")

def checkUserData():
    print("\nOpción elegida: \033[92mver mis datos y estadísticas\033[0m.")

    url = EXTERNAL_SERVICE_URL + f"u/{userID}/stats"
    currentDate = datetime.datetime.now().strftime("%Y-%m-%d")
    authToken = genAuthToken(url, currentDate, userToken)

    headers = {
        "User": userID,
        "Date": currentDate,
        "Auth-Token": authToken
    }

    response = requests.get(url, headers=headers)

    if response.status_code == 200:
        statsJSON = response.json()
        createdConvs = statsJSON.get('createdConvs', 0)
        promptCalls = statsJSON.get('promptCalls', 0)
    else:
        print("\033[91mHa ocurrido un error al obtener tus estadísticas.\033[0m")
        createdConvs = 0
        promptCalls = 0

    print(f"Email actual: {userEmail}")
    print(f"Nombre de usuario: {username}")
    print(f"Conversaciones creadas: {createdConvs}")
    print(f"Llamadas al prompt: {promptCalls}\n")

def deleteUser():
    print("\nOpción elegida: \033[92meliminar mi usuario\033[0m.")
    print("¿Estás seguro de que quieres eliminar tu usuario? Esta acción es irreversible.")
    print("1. Sí")
    print("2. No")
    choice = input("Opción elegida: ")

    if choice == "1":
        print("\033[91mEliminando usuario...\033[0m")
        
        url = EXTERNAL_SERVICE_URL + f"u/deleteUser/{userID}"
        currentDate = datetime.datetime.now().strftime("%Y-%m-%d")
        authToken = genAuthToken(url, currentDate, userToken)

        headers = {
            "User": userID,
            "Date": currentDate,
            "Auth-Token": authToken
        }

        response = requests.delete(url, headers=headers)
        if response.status_code == 200:
            print("Usuario \033[92meliminado\033[0m. Gracias por usar este servicio.")
            print("\033[91mSaliendo...\033[0m")
            sys.exit()
        else:
            print("\033[91mHa ocurrido un error al eliminar el usuario. Por favor, inténtalo de nuevo\033[0m\n")
    elif choice == "2":
        return
    else:
        print("Opción \033[91mno válida\033[0m. Volverás al menú principal\n")
        return
    
def genAuthToken(url, date, userToken):
    decodedStr = f"{url}{date}{userToken}"
    md5String = hashlib.md5(decodedStr.encode())
    return md5String.hexdigest()

def clearScreen():
    if os.name == 'nt':
        os.system('cls')
    else:
        os.system('clear')

if __name__ == "__main__":
    main()
