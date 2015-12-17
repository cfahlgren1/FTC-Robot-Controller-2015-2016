package com.qualcomm.ftcrobotcontroller.opmodes;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorController;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.Range;

/**
 * TeleOp Mode
 * <p>
 * Enables control of the robot via the gamepad
 */
public class BasicDriveAndArm extends OpMode {


    DcMotorController.DeviceMode devMode;
    DcMotorController wheels;
    DcMotor motorRight;
    DcMotor motorLeft;
    DcMotor motorArm;
    Servo servoLift;

    int numOpLoops = 1;

    double clawPosition;
    //double LEFT_CLOSE_POSITION;

    double clawDelta = 0.01;
    //double LEFT_CLOSE_POSITION_DELTA = 0.01;

    Servo claw;

    /*
     * Code to run when the op mode is first enabled goes here
     * @see com.qualcomm.robotcore.eventloop.opmode.OpMode#init()
     */
    @Override
    public void init() {
        motorRight = hardwareMap.dcMotor.get("motorLeft");
        motorLeft = hardwareMap.dcMotor.get("motorRight");
        motorArm = hardwareMap.dcMotor.get("motorArm");
        wheels = hardwareMap.dcMotorController.get("wheels");
        // Make sure Servo Controller in Configuration File (on RC android) is "Servo"
        claw = hardwareMap.servo.get("servo_1"); // channel 1
        //servoLift = hardwareMap.servo.get("servoLift");
    }

    /*
     * Code that runs repeatedly when the op mode is first enabled goes here
     * @see com.qualcomm.robotcore.eventloop.opmode.OpMode#init_loop()
     */
    @Override
    public void init_loop() {

        devMode = DcMotorController.DeviceMode.WRITE_ONLY;

        // Reverse one of the motors due to mirror imaged mounting
        motorRight.setDirection(DcMotor.Direction.REVERSE);
        //motorLeft.setDirection(DcMotor.Direction.REVERSE);

        // set the mode
        // Nxt devices start up in "write" mode by default, so no need to switch device modes here.
        motorLeft.setChannelMode(DcMotorController.RunMode.RUN_WITHOUT_ENCODERS);
        motorRight.setChannelMode(DcMotorController.RunMode.RUN_WITHOUT_ENCODERS);

        clawPosition = 0.5;


    }

    /*
     * This method will be called repeatedly in a loop
     * @see com.qualcomm.robotcore.eventloop.opmode.OpMode#loop()
     */
    //This code will program bucket to go up when pressing Y and down with pressing A
    @Override
    public void loop() {

        // The op mode should only use "write" methods (setPower, setChannelMode, etc) while in
        // WRITE_ONLY mode or SWITCHING_TO_WRITE_MODE
        if (allowedToWrite()) {
    /*
     * Gamepad 1 controls the motors via the left stick
     */
            if (gamepad1.dpad_left) {
                // Nxt devices start up in "write" mode by default, so no need to switch modes here.
                motorLeft.setChannelMode(DcMotorController.RunMode.RUN_WITHOUT_ENCODERS);
                motorRight.setChannelMode(DcMotorController.RunMode.RUN_WITHOUT_ENCODERS);
            }
//            if (gamepad1.dpad_right) {
//                // Nxt devices start up in "write" mode by default, so no need to switch modes here.
//                motorLeft.setChannelMode(DcMotorController.RunMode.RUN_USING_ENCODERS);
//                motorRight.setChannelMode(DcMotorController.RunMode.RUN_USING_ENCODERS);
//            }
            //Setting the game pad controls for left open and close flap
            // update the position of the claw
            if (gamepad2.x) {
                //Closes Right Claw
                clawPosition -= clawDelta;
            }
            if (gamepad2.b) {
                // Opens Claw
                clawPosition += clawDelta;
            }

            // clip the position values so that they never exceed 0..1
            clawPosition = Range.clip(clawPosition, 0, 1);

            // write position values to the wrist and claw servo
            claw.setPosition(clawPosition);


            // this is code that makes robot move with gamepad
            // throttle:  left_stick_y ranges from -2 to 2, where -2 is full up,  and 1 is full down
            // direction: left_stick_x ranges from -2 to 2, where -2 is full left and 1 is full right
            float throttle = -gamepad1.left_stick_y;
            float direction = gamepad1.left_stick_x;
            double right = throttle - direction;
            double left = throttle + direction;

            // clip the right/left values so that the values never exceed +/- 1
            right = Range.clip(right, -0.4, 0.4);
            left = Range.clip(left, -0.4, 0.4);

            // write the values to the motors
            motorRight.setPower(right);
            motorLeft.setPower(left);
    /*
     * Gamepad 2 controls the servos via the right stick
     */
            // Motor Control Code using Gamepad 2
            //This code will control the up and down movement of the lift bucket
            if (gamepad2.y) {
                // Lowers Arm
                motorArm.setPower(0.2);
            } else if (gamepad2.a) {
                //Raises Arm
                motorArm.setPower(-0.2);
            } else {
                motorArm.setPower(0);
            }
        }

        // To read any values from the NXT controllers, we need to switch into READ_ONLY mode.
        // It takes time for the hardware to switch, so you can't switch modes within one loop of the
        // op mode. Every 17th loop, this op mode switches to READ_ONLY mode, and gets the current power.
        if (numOpLoops % 17 == 0){
            // Note: If you are using the NxtDcMotorController, you need to switch into "read" mode
            // before doing a read, and into "write" mode before doing a write. This is because
            // the NxtDcMotorController is on the I2C interface, and can only do one at a time. If you are
            // using the USBDcMotorController, there is no need to switch, because USB can handle reads
            // and writes without changing modes. The NxtDcMotorControllers start up in "write" mode.
            // This method does nothing on USB devices, but is needed on Nxt devices.
            wheels.setMotorControllerDeviceMode(DcMotorController.DeviceMode.READ_ONLY);
        }

        // Every 17 loops, switch to read mode so we can read data from the NXT device.
        // Only necessary on NXT devices.
        if (wheels.getMotorControllerDeviceMode() == DcMotorController.DeviceMode.READ_ONLY) {

            // Update the reads after some loops, when the command has successfully propagated through.
            telemetry.addData("Text", "free flow text");
            telemetry.addData("left motor", motorLeft.getPower());
            telemetry.addData("right motor", motorRight.getPower());
            telemetry.addData("RunMode: ", motorLeft.getChannelMode().toString());
            telemetry.addData("servo Position:", claw.getPosition());

            // Only needed on Nxt devices, but not on USB devices
            wheels.setMotorControllerDeviceMode(DcMotorController.DeviceMode.WRITE_ONLY);

            // Reset the loop
            numOpLoops = 0;
        }

        // Update the current devMode
        devMode = wheels.getMotorControllerDeviceMode();
        numOpLoops++;
    }

    // If the device is in either of these two modes, the op mode is allowed to write to the HW.
    private boolean allowedToWrite(){
        return (devMode == DcMotorController.DeviceMode.WRITE_ONLY);
    }
}
