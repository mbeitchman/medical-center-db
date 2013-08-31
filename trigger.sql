-- Marc Beitchman
-- csep544
-- HW5

CREATE OR REPLACE FUNCTION remove_discontinued() RETURNS TRIGGER AS $discontinued$
  DECLARE
    empty_rec RECORD;
  BEGIN
	-- If the quantity of a product in stock goes down to 0
	IF NEW.quantity = 0 THEN
        -- Verify there is still a suplier for the product
        SELECT INTO empty_rec * FROM Supplies splies, Supplier splier WHERE splies.eid = NEW.eid and splies.sid = splier.sid;
        -- If there is no supplier, delete the product which will automatically delete the product from the Stock table 
		IF NOT FOUND THEN
           DELETE FROM Product WHERE eid = NEW.eid;
		END IF;
	END IF;
	RETURN NULL;
  END;
$discontinued$ LANGUAGE plpgsql;

-- The trigger then needs to be registered with the following statement:
 CREATE TRIGGER discontinuedsample AFTER UPDATE ON Stock
    FOR EACH ROW EXECUTE PROCEDURE remove_discontinued();
