document.addEventListener('DOMContentLoaded', function() {
    const updateButton = document.getElementById('updateButton');
    const inputs = document.querySelectorAll('.form-control');
    const initialStates = {};

    // Guardar el estado inicial de los campos
    inputs.forEach(input => {
        initialStates[input.id] = input.value;
    });

    function checkForChanges() {
        let isChanged = false;
        inputs.forEach(input => {
            if (input.value !== initialStates[input.id]) {
                isChanged = true;
            }
        });
        updateButton.disabled = !isChanged;
    }

    // Agregar listener a cada campo para detectar cambios
    inputs.forEach(input => {
        input.addEventListener('input', checkForChanges);
    });
});


document.getElementById('deleteForm').addEventListener('submit', function(event) {
    var btn = document.getElementById('deleteButton');
    var isConfirm = btn.getAttribute('data-confirm') === 'true';
    
    if (!isConfirm) {
        event.preventDefault(); // Esta función detiene el envío del formulario
        btn.innerText = 'Cuidado: pulsa de nuevo para confirmar';
        btn.classList.remove('btn-warning');
        btn.classList.add('btn-danger');
        btn.setAttribute('data-confirm', 'true');
    }
});