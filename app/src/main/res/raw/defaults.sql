INSERT INTO BeetMagnitude VALUES(1, 'Repetitions', 'Number of repetitions', "Reps");
INSERT INTO BeetMagnitude VALUES(2, 'Distance', 'Distance in meters', "Meters");
INSERT INTO BeetMagnitude VALUES(3, 'Duration', 'Time in seconds', "Seconds");

INSERT INTO BeetResistance VALUES(1, 'Overhang', 'Some overhang expressed in degrees where 0 degrees is flat on the floor', 'degrees');
INSERT INTO BeetResistance VALUES(2, 'Grams', 'Additional weight in the form of some number of grams', 'g');
INSERT INTO BeetResistance VALUES(3, 'Elastic', 'Elastic of some resistance factor', 'none');
INSERT INTO BeetResistance VALUES(4, 'Edge', 'Performing an exercise of some kind, but using an edge of some number of mm', 'mm');

-- Bodyweight and Climbing
INSERT INTO BeetExercise VALUES(1, 'Pull Up', 'Pull up exercise', 1);
INSERT INTO BeetExercise VALUES(2, 'Push Up', 'Push up exercise', 1);
INSERT INTO BeetExercise VALUES(3, 'Right hand max edge', 'Using an an edge and chalk, lift the weight', 1);
INSERT INTO BeetExercise VALUES(4, 'Left hand max edge', 'Using an an edge and chalk, lift the weight', 1);
INSERT INTO BeetExercise VALUES(12, 'Campus Board', 'Using a campus board for power training', 1);
INSERT INTO BeetExercise VALUES(9, 'Plank', 'Core stability exercise', 3);
INSERT INTO BeetExercise VALUES(13, 'Fingerboard Hang', 'Hanging from a fingerboard', 3);

-- Strength Training
INSERT INTO BeetExercise VALUES(5, 'Rows', 'Bodyweight or weighted squat', 1);

-- Cardio
INSERT INTO BeetExercise VALUES(10, 'Running', 'Running on a treadmill or outdoors', 2);
INSERT INTO BeetExercise VALUES(11, 'Cycling', 'Cycling on a stationary bike or outdoors', 2);

-- Valid Resistances
INSERT INTO ValidBeetResistances VALUES(1, 2);

-- Right hand, left hand
INSERT INTO ValidBeetResistances VALUES(3, 2), (3, 4);
INSERT INTO ValidBeetResistances VALUES(4, 2), (4, 4);

INSERT INTO ValidBeetResistances VALUES(5, 2);
INSERT INTO ValidBeetResistances VALUES(9, 2);
INSERT INTO ValidBeetResistances VALUES(12, 4);
INSERT INTO ValidBeetResistances VALUES(13, 2), (13, 4);
