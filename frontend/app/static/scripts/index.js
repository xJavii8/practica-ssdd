function ajustarAltura(elemento) {
    const alturaMaxima = 300;
    elemento.style.height = "auto";
    elemento.style.height = Math.min(elemento.scrollHeight, alturaMaxima) + "px";
}

document.addEventListener("DOMContentLoaded", function() {
    // Encuentra el input y el botón por sus elementos
    var input = document.querySelector('input[name="convName"]');
    var button = document.querySelector('#button-addon2');

    // Deshabilitamos el botón, para no crear conversaciones con nombres vacíos
    if (input && button) {
        button.disabled = true;

        input.addEventListener('input', function() {
            // Si hay texto se habilita el botón, de lo contrario, se deshabilita
            button.disabled = !this.value.length;
        });
    }
});