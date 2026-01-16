/// <reference path="../pb_data/types.d.ts" />

/**
 * Stock-Decrement Hook
 *
 * Reduziert automatisch den Bestand eines Produkts wenn ein Kauf erstellt wird.
 *
 * Trigger: Nach Erstellung einer Transaction mit type="purchase"
 * Aktion: product.stockCount -= transaction.quantity
 */

onRecordAfterCreateSuccess((e) => {
    const record = e.record;

    // Nur bei Käufen
    if (record.get("type") !== "purchase") {
        return;
    }

    const productId = record.get("productId");
    const quantity = record.get("quantity") || 1;

    // Kein Produkt verknüpft? Nichts zu tun
    if (!productId) {
        console.warn(`Transaction ${record.id} ist ein Kauf ohne productId`);
        return;
    }

    try {
        // Produkt laden
        const product = $app.findRecordById("products", productId);

        if (!product) {
            console.error(`Produkt ${productId} nicht gefunden`);
            return;
        }

        // Neuen Stock berechnen (mindestens 0)
        const currentStock = product.get("stockCount") || 0;
        const newStock = Math.max(0, currentStock - quantity);

        // Stock aktualisieren
        product.set("stockCount", newStock);
        $app.save(product);

        console.log(`Stock aktualisiert: ${product.get("name")} ${currentStock} -> ${newStock}`);

    } catch (err) {
        console.error(`Fehler beim Stock-Update für Transaction ${record.id}:`, err);
    }

}, "transactions");
